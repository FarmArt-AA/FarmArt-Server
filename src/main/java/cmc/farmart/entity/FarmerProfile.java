package cmc.farmart.entity;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.List;

// TODO:: NoArgsConstructor가 왜 필요한지 확인
@NoArgsConstructor
@Table(name = "A_FARMER_PROFILE")
@Entity
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "farmerProfileId")
    private Long farmerProfileId;

    @OneToOne(fetch = FetchType.LAZY) // 사용자는 하나의 프로필을 가질 수 있다.;
    @JoinColumn(name = "userId")
    User user;

    @Column(name = "farmName")
    private String farmName; // 농가명

    @Column(name = "farmLocation")
    private String farmLocation; // 농사 지역

    @Column(name = "farmIntro")
    private String farmIntroduce; // 농가 소개

    @Column(name = "farmImage1")
    private String farmImage1;

    @Column(name = "farmImage2")
    private String farmImage2;

    @Column(name = "farmImage3")
    private String farmImage3;

    // 재배중인 작물
    @OneToMany(mappedBy = "farmerProfile")
    private List<Crop> crops;

    // 링크
    @OneToMany(mappedBy = "farmerProfile")
    List<ProfileLink> profileLinks;



}
