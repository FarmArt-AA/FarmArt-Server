package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

public class CreateCropDto {

    @Getter
    public static class Request {

        @Schema(description = "작물 이름", required = true)
        private String cropName;

        @Schema(description = "작물 설명", required = true)
        private String cropDescription;

        @Schema(description = "작물 사진 목록", required = false)
        private List<CropImage> cropImages;

    }

    @Data
    public static class CropImage {

        @Schema(description = "작물 사진")
        private String cropImagePath;
    }

    @Data
    @Builder
    public static class Response {


    }
}
