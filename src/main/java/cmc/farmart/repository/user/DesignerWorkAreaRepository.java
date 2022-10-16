package cmc.farmart.repository.user;

import cmc.farmart.entity.designer.DesignerProfile;
import cmc.farmart.entity.designer.DesignerWorkArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DesignerWorkAreaRepository extends JpaRepository<DesignerWorkArea, Long> {

    List<DesignerWorkArea> findAllByDesignerProfile(DesignerProfile designerProfile);

    void deleteAllByDesignerProfile(DesignerProfile designerProfile);
}
