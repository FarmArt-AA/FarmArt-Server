package cmc.farmart.entity.farmer;

import cmc.farmart.entity.AuditableEntity;
import cmc.farmart.entity.ProfileLink;
import cmc.farmart.entity.User;
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

    @OneToOne(fetch = FetchType.LAZY) // 사용자는 하나의 프로필을 가질 수 있다.
    @JoinColumn(name = "a_user_id")
    private User user;

    @Column(name = "famer_profile_image_path")
    private String farmerProfileImagePath; // 농부 프로필 이미지 1개

    @Column(name = "farm_name")
    private String farmName; // 농가명

    @Column(name = "farm_location")
    private String farmLocation; // 농사 지역

    @Column(name = "farm_intro")
    private String farmIntroduce; // 농가 소개

    @OneToMany(mappedBy = "farmerProfile")
    private List<Crop> crops; // 재배중인 작물

    @OneToMany(mappedBy = "farmerProfile")
    private List<FarmProfileImage> farmProfileImages; // 농가 이미지 최대 3개

    @OneToMany(mappedBy = "farmerProfile")
    private List<ProfileLink> profileLinks; // 링크

    // 농부 프로필 이미지 변경 감지
    public void setFarmerProfileImagePath(String farmerProfileImagePath) {
        this.farmerProfileImagePath = farmerProfileImagePath;
    }

    // 마이페이지 조회 시 FarmerProfile 데이터가 없다면 객체 생성
    public FarmerProfile(User user) {
        this.user = user;
    }

    // 농부 프로필 (농가명 ~ 소개) 변경 감지
    public void setFarmerProfile(String farmName, String farmLocation, String farmIntroduce) {
        this.farmName = farmName;
        this.farmLocation = farmLocation;
        this.farmIntroduce = farmIntroduce;
    }
}
