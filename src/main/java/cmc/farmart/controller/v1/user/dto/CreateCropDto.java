package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.util.List;

public class CreateCropDto {

    @Getter
    @Setter
    public static class Request {

        @NotNull(message = "사용자 PK 필수")
        @Schema(description = "사용자 PK")
        private Long userId;

        @Schema(description = "작물 이름", required = true)
        private String cropName;

        @Schema(description = "작물 설명", required = true)
        private String cropDescription;

        @Schema(description = "작물 사진 목록", required = false)
        private List<CropImageFile> cropImages;

    }

    @Data
    public static class CropImageFile {

        @Schema(description = "작물 사진")
        private MultipartFile cropImageFile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "작물 PK")
        private Long cropId;

        @Schema(description = "작물 이름", required = true)
        private String cropName;

        @Schema(description = "작물 설명", required = true)
        private String cropDescription;

        @Schema(description = "작물 사진 목록", required = false)
        private List<CropImageURL> cropImages;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CropImageURL {

        @Schema(description = "작물 사진 PK")
        private Long cropImageId;

        @Schema(description = "작물 사진 URL")
        private String cropImageUrl;

    }
}
