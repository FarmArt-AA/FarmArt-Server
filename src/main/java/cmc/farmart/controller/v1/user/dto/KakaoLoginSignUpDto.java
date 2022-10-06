package cmc.farmart.controller.v1.user.dto;

import cmc.farmart.domain.user.ConfirmationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Set;

public class KakaoLoginSignUpDto {

    @Getter
    public static class Reqeust {

        @Schema(description = "약관 동의 목록")
        Set<ConfirmationType> confirmationTypes;

        @Schema(description = "사용자 이름")
        private String userName;

        @Schema(description = "사용자 닉네임")
        private String userNickName;

    }
}
