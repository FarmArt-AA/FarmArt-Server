package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetCropDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "재배중인 작물")
        private List<Crop> crops;
    }

    @Data
    public static class Crop {

        @Schema(description = "작물 이름", required = true)
        private String cropName;

        @Schema(description = "작물 설명", required = true)
        private String cropDescription;

        @Schema(description = "작물 사진 목록", required = false)
        private List<CropImage> cropImages;

    }

    @Data
    public static class CropImage {

        @Schema(description = "작물 첫번째 사진")
        private String cropImage1;

        @Schema(description = "작물 두번째 사진")
        private String cropImage2;

        @Schema(description = "작물 세번째 사진")
        private String cropImage3;

    }
}
