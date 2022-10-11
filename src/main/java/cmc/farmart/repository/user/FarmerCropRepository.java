package cmc.farmart.repository.user;

import cmc.farmart.entity.farmer.Crop;
import cmc.farmart.entity.farmer.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FarmerCropRepository extends JpaRepository<Crop, Long> {

    List<Crop> findAllByFarmerProfile(FarmerProfile farmerProfile);
}
