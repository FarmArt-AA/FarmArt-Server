package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_FARMER_PROFILE_IMAGE")
@Entity
public class FarmerProfileImage extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_farmer_profile_id")
    private FarmerProfile farmerProfile; // 농부 이미지

    @Column(name = "farm_image_path")
    private String farmImagePath; // 농가 이미지

}
