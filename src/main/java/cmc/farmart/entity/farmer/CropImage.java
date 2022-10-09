package cmc.farmart.entity.farmer;

import cmc.farmart.entity.AuditableEntity;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_CROP_IMAGE")
@Entity
public class CropImage extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_crop_id")
    private Crop crop;

    @Column(name = "crop_image_path")
    private String cropImagePath;
}
