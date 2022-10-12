package cmc.farmart.entity.designer;

import cmc.farmart.entity.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROJECT_IMAGE")
@Entity
public class DesignerProjectImage extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_project_id")
    private DesignerProject designerProject;

    @Column(name = "designer_project_image_path")
    private String designerProjectImagePath;

    public DesignerProjectImage(DesignerProject designerProject) {
        this.designerProject = designerProject;
    }
}
