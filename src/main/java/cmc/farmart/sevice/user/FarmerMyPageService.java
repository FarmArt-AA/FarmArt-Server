package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.UpdateFarmerProfileImageDto;
import cmc.farmart.entity.FarmerProfile;
import cmc.farmart.entity.FileExtensionType;
import cmc.farmart.entity.User;
import cmc.farmart.repository.user.FarmerProfileRepository;
import cmc.farmart.repository.user.UserRepository;
import cmc.farmart.sevice.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FarmerMyPageService {

    private final String FARMAR_ORIGIN_S3_PREFIX_OBJECT_KEY = "/farmer/image";

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;

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
        Optional<FarmerProfile> farmerProfile = farmerProfileRepository.findByUser(user);

        if (Boolean.FALSE.equals(farmerProfile.isPresent())) {
            FarmerProfile newFarmerProfile = FarmerProfile.builder()
                    .user(user)
                    .farmerProfileImagePath(bucketKey)
                    .build();
            farmerProfileRepository.save(newFarmerProfile);
        } else {
            farmerProfile.get().setFarmerProfileImagePath(bucketKey);
            farmerProfileRepository.save(farmerProfile.get());
        }

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
}
