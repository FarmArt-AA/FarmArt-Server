package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

public class GetFarmerProfileIntroduceDto {

    @Data
    @Builder
    public static class Response {

        @Schema(description = "농부 닉네임")
        private String nickName;

        @Schema(description = "농가명")
        private String farmName;

        @Schema(description = "농사 지역")
        private String farmLocation;

        @Schema(description = "농부 소개")
        private String introduce;
    }
}
