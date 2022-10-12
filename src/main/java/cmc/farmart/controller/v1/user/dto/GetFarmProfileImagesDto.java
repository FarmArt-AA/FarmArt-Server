package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetFarmProfileImagesDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private List<FarmProfileImage> farmProfileImages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmProfileImage {

        @Schema(description = "농가 사진 PK")
        private Long cropImageId;

        @Schema(description = "농가 사진 URL")
        private String cropImageUrl;
    }
}
