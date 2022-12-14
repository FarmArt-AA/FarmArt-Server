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

        verifyExistsFile(request.getFile()); // ???????????? ????????? Exception??? ????????????. (????????? ??????)

        String extension = getExtension(Objects.requireNonNull(request.getFile().getOriginalFilename())); // ????????? ??????

        verifyImageExtension(extension); // ???????????? ???????????? ??????????????? ??????

        String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // ????????? ?????? ??? ??????

        // TODO:: Need Code Refactoring
        FarmerProfile farmerProfile = farmerProfileRepository.findByUser(user).orElseThrow(() -> new FarmartException(Status.NOT_FOUND));
        if (Objects.nonNull(farmerProfile.getFarmerProfileImagePath())) { // ??????????????? ???????????? ?????? ??????????????? s3 ?????? ???????????? ???????????? ????????????.
            s3Service.delete(bucketName, farmerProfile.getFarmerProfileImagePath());
        }
        farmerProfile.setFarmerProfileImagePath(bucketKey);

        try {
            // ????????? ??????
            s3Service.put(bucketName, bucketKey, request.getFile().getInputStream());

            String downloadUrl = s3Service.getPresignedUrl4Download(bucketName, bucketKey, presignedUrlExpireMillisecond);

            return UpdateFarmerProfileImageDto.Response.builder()
                    // TODO:: FarmerProfileImage PK??? ????????????
                    .userId(user.getId())
                    .path(downloadUrl)
                    .build();

        } catch (Exception ex) {
            s3Service.delete(bucketName, bucketKey);
            log.error(ex.getMessage());
            throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
        }
    }

    public GetFarmerProfileIntroduceDto.Response getFarmerProfileIntroduce(String userId) { // ?????? ????????? ????????? ~ ?????? ?????? API

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

        // farmerProfile ?????? update
        user.setUserNickName(request.getNickName()); // ????????? ????????? ??????
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

    private String makeBucketKey(final String s3PrefixObjectKey, final String fileExtension) { // key ??????
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

        // ????????? ?????? 5????????? ?????? ??????.
        List<Crop> farmerCropList = farmerCropRepository.findAllByFarmerProfile(farmerProfile);
        checkCropMaxAddSizeExceeded(farmerCropList.size() >= 5, Status.MAX_ADD_SIZE_EXCEEDED);

        // ???????????? ?????? ??????
        Crop crop = Crop.builder()
                .farmerProfile(farmerProfile)
                .cropName(request.getCropName())
                .cropDescription(request.getCropDescription())
                .build();
        farmerCropRepository.save(crop);

        ArrayList<CreateCropDto.CropImageURL> cropImageURLS = new ArrayList<>(); // ?????? ?????? ?????? ????????? ?????? ?????????
        for (CreateCropDto.CropImageFile cropImage : request.getCropImages()) {
            checkCropImageMaxUploadSizeExceeded(cropImageURLS); // ?????? ?????? ?????? ?????? (?????? 3???) ??????

            verifyExistsFile(cropImage.getCropImageFile()); // ???????????? ????????? Exception??? ????????????. (????????? ??????)

            String extension = getExtension(Objects.requireNonNull(cropImage.getCropImageFile().getOriginalFilename())); // ????????? ??????

            verifyImageExtension(extension); // ???????????? ???????????? ??????????????? ??????

            String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // ????????? ?????? ??? ??????

            try {
                // ????????? ??????
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

        // Crop ???????????? ????????? ?????? ??? ????????? ??????, ????????? ?????? ??? Dto mapping
        // ?????? ?????? ?????? ?????? ???: ???????????? ?????? ?????? ?????? ???????????? ?????? ??????.
        // 1. ???????????? ?????? ???????????? ?????? ????????????. by DB
        List<Crop> crops = farmerCropRepository.findAllByFarmerProfile(farmerProfile);

        // 2. DB??? ????????? ?????? ???????????? GetCropDto.Crop ????????? ?????? ???????????????.
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
        // ?????? ????????? ?????????.
        Crop crop = farmerCropRepository.findById(request.getCropId()).orElseThrow(() -> new FarmartException(Status.NOT_EXISTS_CROP));

        // ??????????????? ????????? ?????????, ?????? ????????? ????????????.
        crop.setCropName(request.getCropName());
        crop.setCropDescription(request.getCropDescription());
        farmerCropRepository.save(crop);


        List<CropImage> cropImages = cropImageRepository.findAllByCrop(crop); // Db?????? ???????????? ???????????? ?????? ????????????.

        for (CropImage cropImage : cropImages) {
            try {
                // ?????? DB ?????? buckey?????? ???????????? ???????????? s3?????? ????????????.
                s3Service.delete(bucketName, cropImage.getCropImageUrl());
            } catch (Exception ex) {
                log.error(ex.getMessage());
            }
            // ?????? DB ???????????? ?????? ????????????.
            cropImageRepository.deleteAllCropImageByCrop(crop);
        }


        ArrayList<CreateCropDto.CropImageURL> cropImageURLS = new ArrayList<>(); // ?????? ?????? ?????? ????????? ?????? ?????????
        if (Objects.isNull(request.getCropImages())) { // request??? ?????? ?????? MultipartFile??? ????????? ??? ???????????? ????????????.
            return UpdateCropDto.Response.builder()
                    .cropId(crop.getId())
                    .cropName(crop.getCropName())
                    .cropDescription(crop.getCropDescription())
                    .cropImages(cropImageURLS)
                    .build();
        } else { // rquest??? ?????? ?????? MultipartFile??? ?????? ?????? ???????????????.
            for (UpdateCropDto.CropImageFile cropImage : request.getCropImages()) {
                checkCropImageMaxUploadSizeExceeded(cropImageURLS); // ?????? ?????? ?????? ?????? (?????? 3???) ??????

                verifyExistsFile(cropImage.getCropImageFile()); // ???????????? ????????? Exception??? ????????????. (????????? ??????)

                String extension = getExtension(Objects.requireNonNull(cropImage.getCropImageFile().getOriginalFilename())); // ????????? ??????

                verifyImageExtension(extension); // ???????????? ???????????? ??????????????? ??????

                String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // ????????? ?????? ??? ??????

                try {
                    // ????????? ??????
                    s3Service.put(bucketName, bucketKey, cropImage.getCropImageFile().getInputStream());
                } catch (Exception ex) {
                    s3Service.delete(bucketName, bucketKey);
                    log.error(ex.getMessage());
                    throw new FarmartException(Status.INTERNAL_SERVER_ERROR);
                }

                // DB??? buckeyKey??? ????????????. (?????? ?????? ????????? ??????)
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

        ArrayList<GetFarmProfileImagesDto.FarmProfileImage> farmProfileImages = new ArrayList<>(); // ?????? ????????? ???????????? ???????????? ????????? ????????????
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

        // ???????????? ?????? 3????????? ????????? ???????????? ?????????.
        if (farmProfileImages.size() >= 3) throw new FarmartException(Status.MAX_UPLOAD_SIZE_EXCEEDED);

        verifyExistsFile(request.getFarmImageFile()); // ???????????? ????????? Exception??? ????????????. (????????? ??????)

        String extension = getExtension(Objects.requireNonNull(request.getFarmImageFile().getOriginalFilename())); // ????????? ??????

        verifyImageExtension(extension); // ???????????? ???????????? ??????????????? ??????

        String bucketKey = makeBucketKey(FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // ????????? ?????? ??? ??????

        try {
            // ????????? ??????
            s3Service.put(bucketName, bucketKey, request.getFarmImageFile().getInputStream());

            // ????????? ?????? ????????? ????????? ??????
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
            // ??????????????? ???????????? ??????
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
