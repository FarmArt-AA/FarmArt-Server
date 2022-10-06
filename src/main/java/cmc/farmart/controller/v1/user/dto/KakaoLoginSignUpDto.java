package cmc.farmart.controller.v1.user.dto;

import cmc.farmart.domain.user.ConfirmationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.Set;

public class KakaoLoginSignUpDto {

    @Getter
    public static class Reqeust {

        @NotBlank(message = "약관 '필수 동의' 필수")
        @Schema(description = "약관 동의 목록")
        Set<ConfirmationType> confirmationTypes;

        @NotBlank(message = "사용자 이름 필수")
        @Schema(description = "사용자 이름")
        private String userName;

        @NotBlank(message = "사용자 닉네임 필수")
        @Schema(description = "사용자 닉네임")
        private String userNickName;

        @NotBlank(message = "작업 유형 필수")
        @Schema(description = "직업 유형 [농부: FARMER, 디자이너: DESIGNER]")
        private String jobTitle;

        @Schema(description = "사용자 전화번호('-' 제외)")
        @NotBlank(message = "휴대폰 번호는 필수 입력값 입니다.")
        @Pattern(regexp = "^01([0|1|6|7|8|9])?([0-9]{3,4})?([0-9]{4})$", message = "올바른 휴대폰 번호 형식이 아닙니다.")
        private String phoneNumber;

    }
}
