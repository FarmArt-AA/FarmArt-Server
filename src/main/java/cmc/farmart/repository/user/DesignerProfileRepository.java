package cmc.farmart.repository.user;

import cmc.farmart.entity.User;
import cmc.farmart.entity.designer.DesignerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignerProfileRepository extends JpaRepository<DesignerProfile, Long> {
    Optional<DesignerProfile> findByUser(User user);
}
