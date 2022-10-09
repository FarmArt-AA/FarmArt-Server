package cmc.farmart.entity.designer;

import cmc.farmart.entity.AuditableEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROFILE")
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

    @Column(name = "designer_projectImageUrl1")
    private String designerProjectImageUrl1;

    @Column(name = "designer_projectImageUrl2")
    private String designerProjectImageUrl2;

    @Column(name = "designer_projectImageUrl3")
    private String designerProjectImageUrl3;
}
