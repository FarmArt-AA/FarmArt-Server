package cmc.farmart.controller.v1.user.dto;

import cmc.farmart.entity.designer.DesignerWorkAreaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class GetDesignerProfileIntroduceDto {

    @Data
    @Builder
    public static class Response {

        @Schema(description = "디자이너 닉네임")
        private String nickName;

        @Schema(description = "디자이너 프로필 제목")
        private String designerProfileTitle;

        @Schema(description = "디자이너 소개")
        private String designerProfileIntroduce;

        @Schema(description = "작업 분야 타입 리스트")
        private List<DesignerWorkAreaTypes> designerWorkAreaTypes;
    }

    @Data
    @Builder
    public static class DesignerWorkAreaTypes {

        @Schema(description = "작업 분야 타입")
        private DesignerWorkAreaType designerWorkAreaType;

    }
}
