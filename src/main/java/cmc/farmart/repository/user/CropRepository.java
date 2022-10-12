package cmc.farmart.repository.user;

import cmc.farmart.entity.farmer.Crop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CropRepository extends JpaRepository<Crop, Long> {
}
