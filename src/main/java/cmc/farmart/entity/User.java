package cmc.farmart.entity;


import cmc.farmart.domain.user.JobTitle;
import cmc.farmart.domain.user.SocialType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Getter
@NoArgsConstructor
@Table(name = "A_USER")
@Entity
public class User extends AuditableEntity{

    @Column(name = "jobTitle")
    @Enumerated(EnumType.STRING) // 농부, 디자이너
    private JobTitle jobTitle;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "userName")
    private String userName; // 사용자 본명

    @Column(name = "userNickName")
    private String userNickName; // 서비스 활동명

    @Column(name = "email")
    private String email; // 카카오 로그인 정보로 받고, 동의하지 않으면 추후 마이페이지에서 추가 기입(정보가 있어야 의뢰 가능)

    @Column(name = "socialId", nullable = false)
    private String socialId; // 카카오 아이디 User PK값

    @Column(name = "socialType", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType socialType; // 애플로그인, 카카오 로그인 구분

    @Column(name = "profileImageUrl")
    private String profileImageUrl; // 프로필 사진

    @Column(name = "refreshToken", nullable = false)
    private String refreshToken;

    public User(String email, String socialId, String profileImageUrl, String refreshToken, SocialType socialType) {
        this.email = email;
        this.socialId = socialId;
        this.profileImageUrl = profileImageUrl;
        this.refreshToken = refreshToken;
        this.socialType = socialType;
    }
}
