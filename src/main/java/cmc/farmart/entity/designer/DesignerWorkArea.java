package cmc.farmart.entity.designer;

import cmc.farmart.entity.IdentifiableEntity;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "A_Designer_Work_Area")
@Entity
public class DesignerWorkArea extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desinger_profile_id")
    private DesignerProfile designerProfile;

    @Column(name = "designer_work_area_type")
    @Enumerated(EnumType.STRING)
    private DesignerWorkAreaType designerWorkAreaType;


}
