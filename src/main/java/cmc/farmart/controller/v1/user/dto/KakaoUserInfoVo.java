package cmc.farmart.controller.v1.user.dto;

import cmc.farmart.domain.user.JobTitle;
import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoUserInfoVo {

    private String userName;
    private String userNickName;
    private String email;
    private String socialId;
    private String profileImageUrl;
    private String refreshToken;
    private SocialType socialType;
    private String phoneNumber;
    private JobTitle jobtitle;

    // TODO:: 왜 필요한지 의문
    public KakaoUserInfoVo(String socialId, SocialType socialType, String profileImageUrl) {
        this.socialId = socialId;
        this.socialType = socialType;
        this.profileImageUrl = profileImageUrl;
    }

    public User toEntity() {
        User user = new User(this.userName, this.userNickName, this.email, this.socialId, this.profileImageUrl, this.refreshToken, this.socialType, this.phoneNumber, this.jobtitle);
        return user;
    }
}
