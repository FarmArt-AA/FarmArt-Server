package cmc.farmart.repository.user;

import cmc.farmart.entity.farmer.Crop;
import cmc.farmart.entity.farmer.CropImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CropImageRepository extends JpaRepository<CropImage, Long> {
    List<CropImage> findAllByCrop(Crop crop);

    void deleteAllCropImageByCrop(Crop crop);
}
