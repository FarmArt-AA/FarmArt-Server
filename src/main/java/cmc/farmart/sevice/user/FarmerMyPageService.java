package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.*;
import cmc.farmart.entity.FileExtensionType;
import cmc.farmart.entity.User;
import cmc.farmart.entity.farmer.Crop;
import cmc.farmart.entity.farmer.CropImage;
import cmc.farmart.entity.farmer.FarmerProfile;
import cmc.farmart.repository.user.CropImageRepository;
import cmc.farmart.repository.user.FarmerCropRepository;
import cmc.farmart.repository.user.FarmerProfileRepository;
import cmc.farmart.repository.user.UserRepository;
import cmc.farmart.sevice.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FarmerMyPageService {

    private final String FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY = "/farmer/image";

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final FarmerCropRepository farmerCropRepository;
    private final CropImageRepository cropImageRepository;

    private final ModelMapper modelMapper;


    @Value("${farmart.project.document.bucket}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expire-millisecond}")
    private Long presignedUrlExpireMillisecond;

    public UpdateFarmerProfileImageDto.Response updateFarmerProfileImage(UpdateFarmerProfileImageDto.Request request) {

        User user = getUserById(request.getUserId());

        verifyExistsFile(request.getFile()); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)

        String extension = getExtension(Objects.requireNonNull(request.getFile().getOriginalFilename())); // 확장자 추출

        verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증

        String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

        // TODO:: Need Code Refactoring
        FarmerProfile farmerProfile = farmerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.NOT_FOUND));
        farmerProfile.setFarmerProfileImagePath(bucketKey);

        try {
            // 이미지 등록
            s3Service.put(bucketName, bucketKey, request.getFile().getInputStream());

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);

            return UpdateFarmerProfileImageDto.Response.builder()
                    // TODO:: FarmerProfileImage PK값 전달하기
                    .userId(user.getId())
                    .path(downloadUrl)
                    .build();

        } catch (Exception ex) {
            s3Service.delete(bucketName, bucketKey);
            log.error(ex.getMessage());
            throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public GetFarmerProfileIntroduceDto.Response getFarmerProfileIntroduce(String userId) { // 농부 프로필 닉네임 ~ 소개 조회 API

        User user = getUserById(Long.parseLong(userId));
        Optional<FarmerProfile> farmerProfile = farmerProfileRepository.findByUser(user);
        if (Boolean.FALSE.equals(farmerProfile.isPresent())) {
            farmerProfileRepository.save(new FarmerProfile(user));
            return GetFarmerProfileIntroduceDto.Response.builder()
                    .nickName(user.getUserNickName())
                    .build();
        }
        return responseFarmerProfileIntroduceDto(user, farmerProfile.get());
    }

    public UpdateFarmerProfileIntroduceDto.Response updateFarmerProfileIntroduce(String userId, UpdateFarmerProfileIntroduceDto.Request request) {

        User user = getUserById(Long.parseLong(userId));
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        // farmerProfile 값을 update
        user.setUserNickName(request.getNickName()); // 사용자 닉네임 변경
        farmerProfile.setFarmerProfile(request.getFarmName(), request.getFarmLocation(), request.getFarmIntroduce());


        return UpdateFarmerProfileIntroduceDto.Response.builder()
                .nickName(user.getUserNickName())
                .farmName(farmerProfile.getFarmName())
                .farmLocation(farmerProfile.getFarmLocation())
                .farmIntroduce(farmerProfile.getFarmIntroduce())
                .build();
    }

    private User getUserById(final Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new FarmartException(Status.ACCESS_DENIED));
    }

    private String makeBucketKey(final String s3PrefixObjectKey, final String fileExtension) { // key 생성
        return s3PrefixObjectKey + UUID.randomUUID() + "." + fileExtension;

    }

    private void verifyImageExtension(final String extension) {
        if (extension == null || FileExtensionType.IMAGE.stream().noneMatch(ext -> ext.equals(extension))) {
            throw new FarmartException(Status.IMAGE_FILE_ONLY);
        }
    }

    private String getExtension(final String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex == -1) {
            throw new FarmartException(Status.WITHOUT_FILE_EXTENSION);
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private void verifyExistsFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new FarmartException(Status.REQUIRE_FILE);
        }
    }

    private GetFarmerProfileIntroduceDto.Response responseFarmerProfileIntroduceDto(User user, FarmerProfile farmerProfile) {
        return GetFarmerProfileIntroduceDto.Response.builder()
                .nickName(user.getUserNickName())
                .farmName(farmerProfile.getFarmName())
                .farmLocation(farmerProfile.getFarmLocation())
                .introduce(farmerProfile.getFarmIntroduce())
                .build();
    }

    public CreateCropDto.Response createFarmerCrop(String userId, CreateCropDto.Request request) throws URISyntaxException {

        User user = getUserById(Long.parseLong(userId));
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        List<Crop> farmerCropList = farmerCropRepository.findAllByFarmerProfile(farmerProfile);
        checkCropMaxAddSizeExceeded(farmerCropList.size() > 5, Status.MAX_ADD_SIZE_EXCEEDED);

        // 재배중인 작물 추가
        Crop crop = Crop.builder()
                .farmerProfile(farmerProfile)
                .cropName(request.getCropName())
                .cropDescription(request.getCropDescription())
                .build();
        farmerCropRepository.save(crop);


        ArrayList<CreateCropDto.CropImageURL> cropImageURLS = new ArrayList<>(); // 재배 중인 작물 사진을 담을 리스트
        for (CreateCropDto.CropImageFile cropImage : request.getCropImages()) {
            checkCropImageMaxUploadSizeExceeded(cropImageURLS); // 재배 중인 작물 사진 (최대 3개) 추가

            verifyExistsFile(cropImage.getCropImagePath()); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)

            String extension = getExtension(Objects.requireNonNull(cropImage.getCropImagePath().getOriginalFilename())); // 확장자 추출

            verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증

            String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

            try {
                // 이미지 등록
                s3Service.put(bucketName, bucketKey, cropImage.getCropImagePath().getInputStream());
            } catch (Exception ex) {
                s3Service.delete(bucketName, bucketKey);
                log.error(ex.getMessage());
                throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
            }
            CropImage newCropImage = CropImage.builder()
                    .crop(crop)
                    .cropImageUrl(bucketKey)
                    .build();
            cropImageRepository.save(newCropImage);

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);
            CreateCropDto.CropImageURL cropImageUrl = CreateCropDto.CropImageURL.builder()
                    .cropImageId(newCropImage.getId())
                    .cropImageUrl(downloadUrl)
                    .build();
            cropImageURLS.add(cropImageUrl);
        }
        return CreateCropDto.Response.builder()
                .cropId(crop.getId())
                .cropName(crop.getCropName())
                .cropDescription(crop.getCropDescription())
                .cropImages(cropImageURLS)
                .build();
    }

    private void checkCropMaxAddSizeExceeded(boolean farmerCropList, Status maxAddSizeExceeded) {
        if (farmerCropList) {
            throw new FarmartException(maxAddSizeExceeded);
        }
    }

    private void checkCropImageMaxUploadSizeExceeded(ArrayList<CreateCropDto.CropImageURL> cropImageURLS) {
        checkCropMaxAddSizeExceeded(cropImageURLS.size() == 3, Status.MAX_UPLOAD_SIZE_EXCEEDED);
    }

    public GetCropDto.Response getFarmerCrops(String userId) {

        User user = getUserById(Long.parseLong(userId));
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        // Crop 데이터가 있는지 확인 후 없으면 생성, 있다면 조회 후 Dto mapping
        // 지금 내가 하고 싶은 것: 사용자의 재배 중인 작물 리스트를 주고 싶다.
        // 1. 사용자의 작물 리스트를 전부 가져온다. by DB
        List<Crop> crops = farmerCropRepository.findAllByFarmerProfile(farmerProfile);

        // 2. DB에 저장돤 작물 리스트를 GetCropDto.Crop 타입에 맞게 맵핑해준다.
        List<GetCropDto.Crop> cropList = crops.stream()
                .map(crop -> GetCropDto.Crop.builder()
                        .cropId(crop.getId())
                        .cropName(crop.getCropName())
                        .cropDescription(crop.getCropDescription())
                        .cropImages(
                                crop.getCropImages().stream()
                                        .map(cropImage ->
                                        {
                                            try {
                                                return GetCropDto.CropImage.builder()
                                                        .cropImageId(cropImage.getId())
                                                        .cropImageUrl(s3Service.getPresignedUrl4Download(bucketName, cropImage.getCropImageUrl(), presignedUrlExpireMillisecond))
                                                        .build();
                                            } catch (URISyntaxException e) {
                                                throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
                                            }
                                        }).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());
        return GetCropDto.Response.builder()
                .crop(cropList)
                .build();
    }

    private FarmerProfile getFarmerProfileByUser(User user) {
        return farmerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.BAD_REQUEST));
    }

    public UpdateCropDto.Response updateFarmerCrop(Long userId, Long cropId, UpdateCropDto.Request request) throws URISyntaxException {
        // 해당 작물을 찾는다.
        Crop crop = farmerCropRepository.findById(cropId).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_CROP));
        crop.setCropName(request.getCropName());
        crop.setCropDescription(request.getCropDescription());
        farmerCropRepository.save(crop);


        List<CropImage> cropImages = cropImageRepository.findAllByCrop(crop);
        for (CropImage cropImage : cropImages) {
            try {
                // 기존 DB 에서 buckey키를 가져와서 이미지를 s3에서 삭제한다.
                s3Service.delete(bucketName, cropImage.getCropImageUrl());
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            // 기존 DB 항목들을 전체 삭제한다.
            cropImageRepository.deleteCropImageByCrop(crop);
        }

        // rquest를 통해 받은 MultipartFile을 다시 전체 업로드한다.
        ArrayList<CreateCropDto.CropImageURL> cropImageURLS = new ArrayList<>(); // 재배 중인 작물 사진을 담을 리스트
        for (UpdateCropDto.CropImageFile cropImage : request.getCropImages()) {
            checkCropImageMaxUploadSizeExceeded(cropImageURLS); // 재배 중인 작물 사진 (최대 3개) 추가

            verifyExistsFile(cropImage.getCropImageFile()); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)

            String extension = getExtension(Objects.requireNonNull(cropImage.getCropImageFile().getOriginalFilename())); // 확장자 추출

            verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증

            String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

            try {
                // 이미지 등록
                s3Service.put(bucketName, bucketKey, cropImage.getCropImageFile().getInputStream());
            } catch (Exception ex) {
                s3Service.delete(bucketName, bucketKey);
                log.error(ex.getMessage());
                throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
            }

            // DB에 buckeyKey를 저장한다. (작물 추가 로직과 동일)
            CropImage newCropImage = CropImage.builder()
                    .crop(crop)
                    .cropImageUrl(bucketKey)
                    .build();
            cropImageRepository.save(newCropImage);

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);
            CreateCropDto.CropImageURL cropImageUrl = CreateCropDto.CropImageURL.builder()
                    .cropImageId(newCropImage.getId())
                    .cropImageUrl(downloadUrl)
                    .build();
            cropImageURLS.add(cropImageUrl);
        }

        farmerCropRepository.save(crop);

        return UpdateCropDto.Response.builder()
                .cropId(crop.getId())
                .cropName(crop.getCropName())
                .cropDescription(crop.getCropDescription())
                .cropImages(cropImageURLS)
                .build();
    }
}
