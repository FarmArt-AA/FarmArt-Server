package cmc.farmart.repository.user;

import cmc.farmart.entity.farmer.FarmProfileImage;
import cmc.farmart.entity.farmer.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmProfileImageRepository extends JpaRepository<FarmProfileImage, Long> {
    List<FarmProfileImage> findAllByFarmerProfile(FarmerProfile farmerProfile);

    Optional<FarmProfileImage> findByFarmerProfileAndAndId(FarmerProfile farmerProfile, Long farmerProfileImageId);

}
