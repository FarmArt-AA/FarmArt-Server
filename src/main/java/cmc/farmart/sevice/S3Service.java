package cmc.farmart.sevice;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.entity.FileExtensionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public Boolean put(final String bucketName, final String bucketKey, final InputStream is) throws IOException {
        try {


            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .acl(ObjectCannedACL.PRIVATE)
                    .bucket(bucketName)
                    .key(bucketKey)
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromInputStream(is, is.available()));
            return true;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    public String getPresignedUrl4Download(final String bucketName, final String bucketKey, final long millisecond) throws URISyntaxException {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(bucketKey)
                    .build();

            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMillis(millisecond))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Generate the presigned request
            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);

            // Log the presigned URL
            return presignedGetObjectRequest.url().toURI().toASCIIString();
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    public void delete(final String bucketName, final String bucketKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(bucketKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            throw ex;
        }
    }

    public String getBucketKey(MultipartFile request, String ORIGIN_S3_PREFIX_OBJECT_KEY) {
        verifyExistsFile(request); // 이미지가 없다면 Exception을 발생한다. (이미지 필수)
        String extension = getExtension(Objects.requireNonNull(request.getOriginalFilename())); // 확장자 추출
        verifyImageExtension(extension); // 이미지류 확장자만 가능하도록 검증
        String bucketKey = makeBucketKey(ORIGIN_S3_PREFIX_OBJECT_KEY, extension); // 새로운 버킷 키 생성

        return bucketKey;
    }

    private void verifyExistsFile(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new FarmartException(Status.REQUIRE_FILE);
        }
    }

    private void verifyImageExtension(final String extension) {
        if (extension == null || FileExtensionType.IMAGE.stream().noneMatch(ext -> ext.equals(extension))) {
            throw new FarmartException(Status.IMAGE_FILE_ONLY);
        }
    }

    private String makeBucketKey(final String s3PrefixObjectKey, final String fileExtension) { // key 생성
        return s3PrefixObjectKey + UUID.randomUUID() + "." + fileExtension;
    }


    private String getExtension(final String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return fileName.substring(dotIndex + 1).toLowerCase();
    }
}
