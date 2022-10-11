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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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

    public void createFarmerCrop(String userId, CreateCropDto.Request request) {

        User user = getUserById(Long.parseLong(userId));
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        // 재배중인 작물 추가
        Crop crop = Crop.builder()
                .farmerProfile(farmerProfile)
                .cropName(request.getCropName())
                .cropDescription(request.getCropDescription())
                .build();
        farmerCropRepository.save(crop);


        // 재배중인 작물 사진 (최대 3개) 추가

        for (CreateCropDto.CropImage cropImage : request.getCropImages()) {
            verifyExistsFile(cropImage.getCropImagePath()); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)

            String extension = getExtension(Objects.requireNonNull(cropImage.getCropImagePath().getOriginalFilename())); // 확장자 추출

            verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증

            String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

            try {
                // 이미지 등록
                s3Service.put(bucketName, bucketKey, cropImage.getCropImagePath().getInputStream());

                String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);

                CropImage newCropImage = CropImage.builder()
                        .crop(crop)
                        .cropImageUrl(bucketKey)
                        .build();
                cropImageRepository.save(newCropImage);

            } catch (Exception ex) {
                s3Service.delete(bucketName, bucketKey);
                log.error(ex.getMessage());
                throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
            }
        }
    }

    public GetCropDto.Response getFarmerCrops(String userId) {

        User user = getUserById(Long.parseLong(userId));
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        // List는 값이 없어도 빈 리스트로 반환한다.
//        Optional<List<Crop>> farmerCrop = farmerCropRepository.findAllByFarmerProfile(farmerProfile);
//        if(Boolean.FALSE.equals(farmerCrop.isPresent())) {
//            return GetCropDto.Response.builder().build();
//        }

        // Crop 데이터가 있는지 확인 후 없으면 생성, 있다면 조회 후 Dto mapping
        // 지금 내가 하고 싶은 것: 사용자의 재배 중인 작물 리스트를 주고 싶다.
        // 1. 사용자의 작물 리스트를 전부 가져온다. by DB
        List<Crop> crops = farmerCropRepository.findAllByFarmerProfile(farmerProfile);

        // 2. DB에 저장돤 작물 리스트를 GetCropDto.Crop 타입에 맞게 맵핑해준다.
        List<GetCropDto.Crop> cropList = crops.stream()
                .map(crop -> GetCropDto.Crop.builder()
                        .cropName(crop.getCropName())
                        .cropDescription(crop.getCropDescription())
                        .cropImages(
                                crop.getCropImages().stream()
                                        .map(cropImage ->
                                        {
                                            try {
                                                return GetCropDto.CropImage.builder()
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
}
