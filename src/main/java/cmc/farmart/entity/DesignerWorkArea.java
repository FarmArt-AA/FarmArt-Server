package cmc.farmart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name = "A_Designer_Work_Area")
@Entity
public class DesignerWorkArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designerWorkAreaId")
    private Long designerWorkAreaId;

    @Column(name = "designerWorkAreaType")
    @Enumerated(EnumType.STRING)
    private DesignerWorkAreaType designerWorkAreaType;
}
