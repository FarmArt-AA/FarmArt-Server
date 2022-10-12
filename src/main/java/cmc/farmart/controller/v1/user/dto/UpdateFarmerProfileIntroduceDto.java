package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class UpdateFarmerProfileIntroduceDto {

    @Getter
    public static class Request {

        @Schema(description = "농부 PK")
        @NotNull(message = "농부 PK 필수")
        private Long userId;

        @Schema(description = "농부 닉네임", required = true)
        @NotBlank(message = "농부 닉네임 필수")
        private String nickName;

        @Schema(description = "농가명", required = true)
        @NotBlank(message = "농가명 필수")
        private String farmName;

        @Schema(description = "농사 지역", required = true)
        @NotBlank(message = "농사 지역 필수")
        private String farmLocation;

        @Schema(description = "농부 소개", required = true)
        @NotBlank(message = "소개 내용 필수")
        private String farmIntroduce;
    }

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
        private String farmIntroduce;
    }
}
