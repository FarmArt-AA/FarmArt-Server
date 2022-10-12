package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateFarmerProfileLinkDto {

    @Getter
    public static class Request {

        @NotNull(message = "사용자 PK 필수")
        @Schema(description = "사용자 PK")
        private Long userId;

        @NotNull(message = "링크 PK 필수")
        @Schema(description = "링크 PK")
        private Long linkId;

        @NotBlank(message = "링크 제목 필수")
        @Schema(description = "링크 제목")
        private String linkTitle;

        @NotBlank(message = "링크 웹사이트 URL 필수")
        @Schema(description = "웹사이트 URL")
        private String webSiteUrl;
    }


    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "링크 PK")
        private Long linkId;

        @Schema(description = "링크 제목")
        private String linkTitle;

        @Schema(description = "웹사이트 URL")
        private String webSiteUrl;

    }
}
