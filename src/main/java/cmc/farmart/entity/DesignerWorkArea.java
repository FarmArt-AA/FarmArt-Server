package cmc.farmart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name = "A_Designer_Work_Area")
@Entity
public class DesignerWorkArea extends IdentifiableEntity {

    @Column(name = "designer_work_area_type")
    @Enumerated(EnumType.STRING)
    private DesignerWorkAreaType designerWorkAreaType;
}
