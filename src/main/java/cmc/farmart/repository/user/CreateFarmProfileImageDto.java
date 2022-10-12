package cmc.farmart.repository.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class CreateFarmProfileImageDto {

    @Getter
    @Setter
    public static class Request {

        @Schema(description = "농부 PK")
        @NotNull(message = "농부 PK 필수")
        private Long userId;

        @Schema(description = "농부의 농가 사진")
        private MultipartFile farmImageFile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "농부의 농가 이미지 등록 사진 URL")
        private String farmProfileImageDownloadUrl;
    }
}
