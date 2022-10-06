package cmc.farmart.entity;

import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;

import javax.persistence.*;
import java.util.List;

// TODO:: NoArgsConstructor가 왜 필요한지 확인
@NoArgsConstructor
@Table(name = "A_FARMER_PROFILE")
@Entity
public class FarmerProfile  extends AuditableEntity{

    @OneToOne(fetch = FetchType.LAZY) // 사용자는 하나의 프로필을 가질 수 있다.;
    @JoinColumn(name = "a_user_id")
    User user;

    @Column(name = "farm_name")
    private String farmName; // 농가명

    @Column(name = "farm_location")
    private String farmLocation; // 농사 지역

    @Column(name = "farm_intro")
    private String farmIntroduce; // 농가 소개

    @Column(name = "farm_image1")
    private String farmImage1;

    @Column(name = "farm_image2")
    private String farmImage2;

    @Column(name = "farm_image3")
    private String farmImage3;

    // 재배중인 작물
    @OneToMany(mappedBy = "farmerProfile")
    private List<Crop> crops;

    // 링크
    @OneToMany(mappedBy = "farmerProfile")
    List<ProfileLink> profileLinks;



}
