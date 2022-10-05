package cmc.farmart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROFILE")
@Entity
public class DesignerProfile extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    User user;

    private String designerProfileTitle;

    private String designerProfileProfileIntroduce;

    // 작업분야

    private String designerProfileImageUrl;

    // 디자이너 프로젝트
    @OneToMany(mappedBy = "designerProfile")
    List<DesignerProject> designerProjects;


}
