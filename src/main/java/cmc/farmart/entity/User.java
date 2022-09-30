package cmc.farmart.entity;


import cmc.farmart.domain.user.SocialType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Getter
@NoArgsConstructor
@Table(name = "F_USER")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "socialId", nullable = false)
    private String socialId; // 카카오 아이디 User PK값

    @Column(name = "socialType", nullable = false)
    @Enumerated(EnumType.STRING)
    private SocialType socialType; // 애플로그인, 카카오 로그인 구분

    @Column(name = "imgUrl", nullable = true)
    private String imgUrl; // 프로필 사진

    @Column(name = "refreshToken", nullable = false)
    private String refreshToken;

    public User(String username, String email, String socialId, String imgUrl, String refreshToken, SocialType socialType) {
        this.username = username;
        this.email = email;
        this.socialId = socialId;
        this.imgUrl = imgUrl;
        this.refreshToken = refreshToken;
        this.socialType = socialType;
    }
}
