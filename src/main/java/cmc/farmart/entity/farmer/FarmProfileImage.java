package cmc.farmart.entity.farmer;

import cmc.farmart.entity.AuditableEntity;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_FARM_PROFILE_IMAGE")
@Entity
public class FarmProfileImage extends AuditableEntity { // 농장 사진 최대 3개

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_farmer_profile_id")
    private FarmerProfile farmerProfile;

    @Column(name = "a_farm_image_path")
    private String farmImagePath; // 농장 사진

    public FarmProfileImage(FarmerProfile farmerProfile) {
        this.farmerProfile = farmerProfile;
    }
}
