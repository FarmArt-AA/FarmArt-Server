package cmc.farmart.entity.farmer;

import cmc.farmart.entity.AuditableEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "A_CROP")
@Entity
public class Crop extends AuditableEntity { // 재배중인 작물

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_farmer_profile_id")
    private FarmerProfile farmerProfile;

    @Column(name = "crop_name")
    private String cropName;

    @Column(name = "crop_description")
    private String cropDescription;

    @OneToMany(mappedBy = "crop")
    private List<CropImage> cropImages;

    public Crop(FarmerProfile farmerProfile) {
        this.farmerProfile = farmerProfile;
    }
}
