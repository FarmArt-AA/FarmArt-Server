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
public class User extends AuditableEntity {

    @Column(name = "job_title")
    @Enumerated(EnumType.STRING) // 농부, 디자이너
    private JobTitle jobTitle;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "user_name")
    private String userName; // 사용자 본명

    @Column(name = "user_nick_name")
    private String userNickName; // 서비스 활동명

    @Column(name = "email")
    private String email; // 카카오 로그인 정보로 받고, 동의하지 않으면 추후 마이페이지에서 추가 기입(정보가 있어야 의뢰 가능)

    @Column(name = "social_id", nullable = false)
    private String socialId; // 카카오 아이디 User PK값

    @Column(name = "social_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType socialType; // 애플로그인, 카카오 로그인 구분

    @Column(name = "profile_image_url")
    private String profileImageUrl; // 프로필 사진

    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;

    public User(String userName, String userNickName, String email, String socialId, String profileImageUrl, String refreshToken, SocialType socialType, String phoneNumber, JobTitle jobTitle) {
        this.userName = userName;
        this.userNickName = userNickName;
        this.email = email;
        this.socialId = socialId;
        this.profileImageUrl = profileImageUrl;
        this.refreshToken = refreshToken;
        this.socialType = socialType;
        this.phoneNumber = phoneNumber;
        this.jobTitle = jobTitle;
    }
}
