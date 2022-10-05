package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_CROP")
@Entity
public class Crop extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    @Column(name = "cropImage1")
    private String cropImage1;

    @Column(name = "cropImage2")
    private String cropImage2;

    @Column(name = "cropImage3")
    private String cropImage3;
}
