package cmc.farmart.entity.designer;

import cmc.farmart.entity.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROJECT")
@Entity
public class DesignerProject extends AuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_profile_id")
    private DesignerProfile designerProfile;

    @Column(name = "designer_project_title")
    private String designerProjectTitle;

    @Column(name = "designer_project_start_date")
    private LocalDateTime designerProjectStartDate;

    @Column(name = "designer_projectEndDate")
    private LocalDateTime designerProjectEndDate;

    @Column(name = "designer_projectIntroduce")
    private String designerProjectIntroduce;

    @OneToMany(mappedBy = "designerProject")
    private List<DesignerProjectImage> designerProjectImages;

    public DesignerProject(DesignerProfile designerProfile) {
        this.designerProfile = designerProfile;
    }
}
