package cmc.farmart.repository.user;

import cmc.farmart.entity.User;
import cmc.farmart.entity.farmer.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FarmerProfileRepository extends JpaRepository<FarmerProfile, Long> {

    Optional<FarmerProfile> findByUser(User user);
}

