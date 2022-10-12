package cmc.farmart.repository.user;

import cmc.farmart.entity.ProfileLink;
import cmc.farmart.entity.designer.DesignerProfile;
import cmc.farmart.entity.farmer.FarmerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileLinkRepository extends JpaRepository<ProfileLink, Long> {

    List<ProfileLink> findAllByFarmerProfile(FarmerProfile farmerProfile);

    List<ProfileLink> findAllByDesignerProfile(DesignerProfile designerProfile);

    Optional<ProfileLink> findByFarmerProfileAndId(FarmerProfile farmerProfile, Long profileLinkId);

    Optional<ProfileLink> findByDesignerProfileAndId(DesignerProfile designerProfile, Long profileLinkId);
}
