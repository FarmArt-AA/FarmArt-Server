package cmc.farmart.controller.v1.user.dto;

import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfoDto {
    private String email;
    private String socialId;
    private String profileImageUrl;
    private String refreshToken;
    private SocialType socialType;

    // TODO:: 왜 필요한지 의문
    public KakaoUserInfoDto(String socialId, SocialType socialType, String profileImageUrl) {
        this.socialId = socialId;
        this.socialType = socialType;
        this.profileImageUrl = profileImageUrl;
    }

    public User toEntity(){
        User user = new User(this.email, this.socialId, this.profileImageUrl, this.refreshToken, this.socialType);
        return user;
    }
}
