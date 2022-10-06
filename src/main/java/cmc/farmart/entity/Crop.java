package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_CROP")
@Entity
public class Crop extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    @Column(name = "crop_image1")
    private String cropImage1;

    @Column(name = "crop_image2")
    private String cropImage2;

    @Column(name = "crop_image3")
    private String cropImage3;
}
