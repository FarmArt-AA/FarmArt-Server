package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.*;
import cmc.farmart.domain.user.JobTitle;
import cmc.farmart.entity.FileExtensionType;
import cmc.farmart.entity.ProfileLink;
import cmc.farmart.entity.User;
import cmc.farmart.entity.designer.DesignerProfile;
import cmc.farmart.entity.farmer.Crop;
import cmc.farmart.entity.farmer.CropImage;
import cmc.farmart.entity.farmer.FarmProfileImage;
import cmc.farmart.entity.farmer.FarmerProfile;
import cmc.farmart.repository.user.*;
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
    private final FarmProfileImageRepository farmProfileImageRepository;
    private final ProfileLinkRepository profileLinkRepository;

    private final DesignerProfileRepository designerProfileRepository;

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
        if (Objects.nonNull(farmerProfile.getFarmerProfileImagePath())) { // 서버로부터 이미지를 받은 이후부터는 s3 기존 이미지를 삭제하고 저장한다.
            s3Service.delete(bucketName, farmerProfile.getFarmerProfileImagePath());
        }
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

    public UpdateFarmerProfileIntroduceDto.Response updateFarmerProfileIntroduce(UpdateFarmerProfileIntroduceDto.Request request) {

        User user = getUserById(request.getUserId());
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
        checkMaxProfileLinkAddSizeExceeded(dotIndex == -1, Status.WITHOUT_FILE_EXTENSION);
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    private void verifyExistsFile(MultipartFile file) {
        checkMaxProfileLinkAddSizeExceeded(Objects.isNull(file) || file.isEmpty(), Status.REQUIRE_FILE);
    }

    private GetFarmerProfileIntroduceDto.Response responseFarmerProfileIntroduceDto(User user, FarmerProfile farmerProfile) {
        return GetFarmerProfileIntroduceDto.Response.builder()
                .nickName(user.getUserNickName())
                .farmName(farmerProfile.getFarmName())
                .farmLocation(farmerProfile.getFarmLocation())
                .introduce(farmerProfile.getFarmIntroduce())
                .build();
    }

    public CreateCropDto.Response createFarmerCrop(CreateCropDto.Request request) throws URISyntaxException {

        User user = getUserById(request.getUserId());
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        // 작물은 최대 5개까지 등록 가능.
        List<Crop> farmerCropList = farmerCropRepository.findAllByFarmerProfile(farmerProfile);
        checkCropMaxAddSizeExceeded(farmerCropList.size() >= 5, Status.MAX_ADD_SIZE_EXCEEDED);

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
        checkMaxProfileLinkAddSizeExceeded(farmerCropList, maxAddSizeExceeded);
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
        return farmerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_FARMER));
    }

    private DesignerProfile getDesignerProfileByUser(User user) {
        return designerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_DESIGNER));
    }

    public UpdateCropDto.Response updateFarmerCrop(UpdateCropDto.Request request) throws URISyntaxException {
        // 해당 작물을 찾는다.
        Crop crop = farmerCropRepository.findById(request.getCropId()).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_CROP));

        // 수정하려는 작물의 작물명, 작물 설명을 수정한다.
        crop.setCropName(request.getCropName());
        crop.setCropDescription(request.getCropDescription());
        farmerCropRepository.save(crop);


        List<CropImage> cropImages = cropImageRepository.findAllByCrop(crop); // Db에서 농작물의 이미지를 모두 가져온다.

        for (CropImage cropImage : cropImages) {
            try {
                // 기존 DB 에서 buckey키를 가져와서 이미지를 s3에서 삭제한다.
                s3Service.delete(bucketName, cropImage.getCropImageUrl());
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            // 기존 DB 항목들을 전체 삭제한다.
            cropImageRepository.deleteAllCropImageByCrop(crop);
        }


        ArrayList<CreateCropDto.CropImageURL> cropImageURLS = new ArrayList<>(); // 재배 중인 작물 사진을 담을 리스트
        if (Objects.isNull(request.getCropImages())) { // request를 통해 받은 MultipartFile이 없다면 빈 리스트를 반환한다.
            return UpdateCropDto.Response.builder()
                    .cropId(crop.getId())
                    .cropName(crop.getCropName())
                    .cropDescription(crop.getCropDescription())
                    .cropImages(cropImageURLS)
                    .build();
        } else { // rquest를 통해 받은 MultipartFile을 다시 전체 업로드한다.
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
        }

        return UpdateCropDto.Response.builder()
                .cropId(crop.getId())
                .cropName(crop.getCropName())
                .cropDescription(crop.getCropDescription())
                .cropImages(cropImageURLS)
                .build();
    }

    public GetFarmProfileImagesDto.Response getFarmProfileImages(Long userId) throws URISyntaxException {

        User user = getUserById(userId);
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        ArrayList<GetFarmProfileImagesDto.FarmProfileImage> farmProfileImages = new ArrayList<>(); // 농가 이미지 다운로드 리스트를 반환할 자료구조
        for (FarmProfileImage farmProfileImage : farmProfileImageRepository.findAllByFarmerProfile(farmerProfile)) {

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, farmProfileImage.getFarmImagePath(), presignedUrlExpireMillisecond);

            GetFarmProfileImagesDto.FarmProfileImage farmProfileImageDto = GetFarmProfileImagesDto.FarmProfileImage.builder()
                    .cropImageId(farmProfileImage.getId())
                    .cropImageUrl(downloadUrl)
                    .build();
            farmProfileImages.add(farmProfileImageDto);

        }

        return GetFarmProfileImagesDto.Response.builder()
                .farmProfileImages(farmProfileImages)
                .build();
    }

    public CreateFarmProfileImageDto.Response createFarmProfileImage(CreateFarmProfileImageDto.Request request) throws URISyntaxException {
        User user = getUserById(request.getUserId());
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);


        List<FarmProfileImage> farmProfileImages = farmProfileImageRepository.findAll();

        // 이미지가 이미 3개라면 더이상 추가하지 못한다.
        if (farmProfileImages.size() >= 3) throw new FarmartException(Status.MAX_UPLOAD_SIZE_EXCEEDED);

        verifyExistsFile(request.getFarmImageFile()); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)

        String extension = getExtension(Objects.requireNonNull(request.getFarmImageFile().getOriginalFilename())); // 확장자 추출

        verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증

        String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

        try {
            // 이미지 등록
            s3Service.put(bucketName, bucketKey, request.getFarmImageFile().getInputStream());

            // 농부의 농가 이미지 데이터 생성
            FarmProfileImage farmProfileImage = FarmProfileImage.builder()
                    .farmerProfile(farmerProfile)
                    .farmImagePath(bucketKey)
                    .build();
            farmProfileImageRepository.save(farmProfileImage);

        } catch (Exception ex) {
            s3Service.delete(bucketName, bucketKey);
            log.error(ex.getMessage());
            throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
        }

        String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);

        return CreateFarmProfileImageDto.Response.builder()
                .farmProfileImageDownloadUrl(downloadUrl)
                .build();
    }

    public void deleteFarmProfileImage(Long userId, Long farmProfileImageId) {
        User user = getUserById(userId);
        FarmerProfile farmerProfile = getFarmerProfileByUser(user);

        Optional<FarmProfileImage> farmerProfileImage = farmProfileImageRepository.findByFarmerProfileAndAndId(farmerProfile, farmProfileImageId);
        checkMaxProfileLinkAddSizeExceeded(Boolean.FALSE.equals(farmerProfileImage.isPresent()), Status.NOT_EXISTS_FARMER_PROFILE);

        s3Service.delete(bucketName, farmerProfileImage.get().getFarmImagePath());
        farmProfileImageRepository.delete(farmerProfileImage.get());

    }

    public GetFarmerProfileLinkDto.Response getFarmerProfileLinks(Long userId) {
        User user = getUserById(userId);
        if (user.getJobTitle().equals(JobTitle.FARMER)) {
            FarmerProfile farmerProfile = getFarmerProfileByUser(user);
            List<ProfileLink> farmerProfileLinks = profileLinkRepository.findAllByFarmerProfile(farmerProfile);

            return GetFarmerProfileLinkDto.Response.builder()
                    .farmerProfileLinkList(farmerProfileLinks.stream()
                            .map(farmerProfileLink -> GetFarmerProfileLinkDto.FarmerProfileLinkList.builder()
                                    .linkId(farmerProfileLink.getId())
                                    .linkTitle(farmerProfileLink.getLinkTitle())
                                    .webSiteUrl(farmerProfileLink.getWebSiteUrl())
                                    .build()).collect(Collectors.toList()))
                    .build();
        } else if (user.getJobTitle().equals(JobTitle.DESIGNER)) {
            // 디자이너의 프로필을 조회
            DesignerProfile designerProfile = getDesignerProfileByUser(user);
            List<ProfileLink> farmerProfileLinks = profileLinkRepository.findAllByDesignerProfile(designerProfile);

            return GetFarmerProfileLinkDto.Response.builder()
                    .farmerProfileLinkList(farmerProfileLinks.stream()
                            .map(designerProfileLink -> GetFarmerProfileLinkDto.FarmerProfileLinkList.builder()
                                    .linkId(designerProfileLink.getId())
                                    .linkTitle(designerProfileLink.getLinkTitle())
                                    .webSiteUrl(designerProfileLink.getWebSiteUrl())
                                    .build()).collect(Collectors.toList()))
                    .build();
        } else {
            throw new FarmartException(Status.NOT_EXISTS_JOB_TITLE);
        }
    }

    public CreateFarmerProfileLinkDto.Response createFarmerProfileLink(CreateFarmerProfileLinkDto.Request request) {
        checkMaxProfileLinkAddSizeExceeded(profileLinkRepository.findAll().size() >= 2, Status.MAX_PROFILE_LINK_ADD_SIZE_EXCEEDED);
        User user = getUserById(request.getUserId());

        if (user.getJobTitle().equals(JobTitle.FARMER)) {
            FarmerProfile farmerProfile = getFarmerProfileByUser(user);
            ProfileLink farmerProfileLink = ProfileLink.builder()
                    .farmerProfile(farmerProfile)
                    .linkTitle(request.getLinkTitle())
                    .webSiteUrl(request.getWebSiteUrl())
                    .build();
            profileLinkRepository.save(farmerProfileLink);

            return CreateFarmerProfileLinkDto.Response.builder()
                    .linkTitle(farmerProfileLink.getLinkTitle())
                    .webSiteUrl(farmerProfileLink.getWebSiteUrl())
                    .build();
        } else if (user.getJobTitle().equals(JobTitle.DESIGNER)) {
            DesignerProfile designerProfile = getDesignerProfileByUser(user);
            ProfileLink designerProfileLink = ProfileLink.builder()
                    .designerProfile(designerProfile)
                    .linkTitle(request.getLinkTitle())
                    .webSiteUrl(request.getWebSiteUrl())
                    .build();
            profileLinkRepository.save(designerProfileLink);

            return CreateFarmerProfileLinkDto.Response.builder()
                    .linkTitle(designerProfileLink.getLinkTitle())
                    .webSiteUrl(designerProfileLink.getWebSiteUrl())
                    .build();
        } else {
            throw new FarmartException(Status.NOT_EXISTS_JOB_TITLE);
        }

    }

    private void checkMaxProfileLinkAddSizeExceeded(boolean profileLinkRepository, Status maxProfileLinkAddSizeExceeded) {
        if (profileLinkRepository) {
            throw new FarmartException(maxProfileLinkAddSizeExceeded);
        }
    }

    public UpdateFarmerProfileLinkDto.Response updateFarmerProfileLink(UpdateFarmerProfileLinkDto.Request request) {
        User user = getUserById(request.getUserId());
        if (user.getJobTitle().equals(JobTitle.FARMER)) {
            FarmerProfile farmerProfile = getFarmerProfileByUser(user);
            ProfileLink farmerProfileLink = profileLinkRepository.findByFarmerProfileAndId(farmerProfile, request.getLinkId()).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_FARMER_PROFILE_LINK));
            farmerProfileLink.setLinkTitle(request.getLinkTitle());
            farmerProfileLink.setWebSiteUrl(request.getWebSiteUrl());
            profileLinkRepository.save(farmerProfileLink);

            return UpdateFarmerProfileLinkDto.Response.builder()
                    .linkId(farmerProfileLink.getId())
                    .linkTitle(farmerProfileLink.getLinkTitle())
                    .webSiteUrl(farmerProfileLink.getWebSiteUrl())
                    .build();

        } else if (user.getJobTitle().equals(JobTitle.DESIGNER)) {
            DesignerProfile designerProfile = getDesignerProfileByUser(user);
            ProfileLink designerProfileLink = profileLinkRepository.findByDesignerProfileAndId(designerProfile, request.getLinkId()).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_FARMER_PROFILE_LINK));
            designerProfileLink.setLinkTitle(request.getLinkTitle());
            designerProfileLink.setWebSiteUrl(request.getWebSiteUrl());
            profileLinkRepository.save(designerProfileLink);
            return UpdateFarmerProfileLinkDto.Response.builder()
                    .linkId(designerProfileLink.getId())
                    .linkTitle(designerProfileLink.getLinkTitle())
                    .webSiteUrl(designerProfileLink.getWebSiteUrl())
                    .build();
        } else {
            throw new FarmartException(Status.NOT_EXISTS_JOB_TITLE);
        }

    }
}
