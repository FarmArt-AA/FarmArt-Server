package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetFarmerProfileLinkDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        @Schema(description = "농부 프로필의 링크 리스트")
        private List<FarmerProfileLinkList> farmerProfileLinkList;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FarmerProfileLinkList {

        @Schema(description = "링크 PK")
        private Long linkId;

        @Schema(description = "링크 제목")
        private String linkTitle;

        @Schema(description = "웹사이트 URL")
        private String webSiteUrl;

    }
}
