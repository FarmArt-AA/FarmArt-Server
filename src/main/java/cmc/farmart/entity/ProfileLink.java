package cmc.farmart.entity;

import cmc.farmart.entity.farmer.FarmerProfile;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_PROFILE_LINK")
@Entity
public class ProfileLink extends AuditableEntity { // 디자이너, 농부 프로필 링크

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    // TODO:: Designer Profile과 연관관계
}
