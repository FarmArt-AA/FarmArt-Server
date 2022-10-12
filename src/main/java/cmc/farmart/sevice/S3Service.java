package cmc.farmart.sevice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
}
