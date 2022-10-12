package cmc.farmart.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum SocialType {
    KAKAO(1, "카카오 로그인"),
    NAVER(2, "네이버 로그인"),
    APPLE(3, "애플 로그인");

    private final Integer socialTypeCode;
    private final String socialTypeName;

    public static SocialType ofSocialTypeCode(int socialTypeCode) {
        return Arrays.stream(values())
                .filter(socialType -> socialType.getSocialTypeCode() == socialTypeCode)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 SocialType 입니다."));
    }
}
