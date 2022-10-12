package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class UpdateCropDto {

    @Getter
    @Setter
    public static class Request {

        @Schema(description = "작물 이름", required = true)
        private String cropName;

        @Schema(description = "작물 설명", required = true)
        private String cropDescription;

        @Schema(description = "작물 사진 목록", required = false)
        private List<CropImageFile> cropImages;

    }

    @Data
    public static class CropImageFile {

        @Schema(description = "작물 사진 URL")
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
        private List<CreateCropDto.CropImageURL> cropImages;

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
