package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public class CreateDesignerProjectDto {

    @Getter
    @Setter
    public static class Request {

        @Schema(description = "디자이너 PK")
        @NotNull(message = "디자이너 PK 필수")
        private Long userId;

        @Schema(description = "디자이너 프로젝트 이름")
        @NotNull(message = "디자이너 프로젝트 이름 필수")
        private String designerProjectTitle;

        @Schema(description = "디자이너 프로젝트 시작 날짜")
        @NotNull(message = "디자이너 프로젝트 시작 날짜 필수")
        private LocalDateTime designerProjectStartDate;

        @Schema(description = "디자이너 프로젝트 종료 날짜")
        @NotNull(message = "디자이너 프로젝트 종료 날짜 필수")
        private LocalDateTime designerProjectEndDate;

        @Schema(description = "디자이너 프로젝트 설명")
        @NotNull(message = "디자이너 프로젝트 설명 필수")
        private String designerProjectIntroduce;

        @Schema(description = "디자이너 프로젝트 사진 리스트")
        private List<DesignerProjectImageFiles> designerProjectImages;
    }

    @Data
    public static class DesignerProjectImageFiles {

        @Schema(description = "디자이너 프로젝트 이미지 파일")
        private MultipartFile designerProjectImageFile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "디자이너 프로젝트 이름")
        private String designerProjectTitle;

        @Schema(description = "디자이너 프로젝트 시작 날짜")
        private LocalDateTime designerProjectStartDate;

        @Schema(description = "디자이너 프로젝트 종료 날짜")
        private LocalDateTime designerProjectEndDate;

        @Schema(description = "디자이너 프로젝트 설명")
        private String designerProjectIntroduce;

        @Schema(description = "디자이너 프로젝트 사진 리스트")
        private List<DesignerProjectImageURL> designerProjectImages;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DesignerProjectImageURL {

        @Schema(description = "디자이너 프로젝트 이미지 PK")
        private Long designerProjectImageId;

        @Schema(description = "디자이너 프로젝트 이미지 URL")
        private String designerProjectImageURL;
    }
}
