package cmc.farmart.controller.v1.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KakaoLoginDto {

    @Schema(description = "사용자 PK")
    private Long userId;

    @Schema(description = "사용자 이메일")
    private String email;

    @Schema(description = "사용자 프로필 이미지")
    private String profileImageUrl;
}
