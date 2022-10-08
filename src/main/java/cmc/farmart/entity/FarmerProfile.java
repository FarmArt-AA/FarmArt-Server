package cmc.farmart.entity;

import lombok.*;

import javax.persistence.*;
import java.util.List;

// TODO:: NoArgsConstructor가 왜 필요한지 확인
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "A_FARMER_PROFILE")
@Setter // For 변경 감지
@Getter
@Entity
public class FarmerProfile extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY) // 사용자는 하나의 프로필을 가질 수 있다.;
    @JoinColumn(name = "a_user_id")
    private User user;

    @Column(name = "farm_name")
    private String farmName; // 농가명

    @Column(name = "farm_location")
    private String farmLocation; // 농사 지역

    @Column(name = "farm_intro")
    private String farmIntroduce; // 농가 소개

    @Column(name = "famer_profile_image_path")
    private String farmerProfileImagePath;

    @OneToMany(mappedBy = "farmImagePath")
    private List<FarmerProfileImage> farmerProfileImages; // 농가 이미지 최대 3개

    // 재배중인 작물
    @OneToMany(mappedBy = "farmerProfile")
    private List<Crop> crops;

    // 링크
    @OneToMany(mappedBy = "farmerProfile")
    private List<ProfileLink> profileLinks;

    // 농부 프로필 이미지 변경 감지
    public void setFarmerProfileImagePath(String farmerProfileImagePath) {
        this.farmerProfileImagePath = farmerProfileImagePath;
    }
}
