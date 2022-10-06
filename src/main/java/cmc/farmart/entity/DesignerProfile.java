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
    @JoinColumn(name = "a_user_id")
    private User user;

    @Column(name = "designer_profile_title")
    private String designerProfileTitle;

    @Column(name = "designer_profile_profile_introduce")
    private String designerProfileProfileIntroduce;

    // 작업분야
    @OneToMany(mappedBy = "designerWorkAreaType")
    private List<DesignerWorkArea> designerWorkAreaTypes;

    @Column(name = "designer_profile_image_url")
    private String designerProfileImageUrl;

    // 디자이너 프로젝트
    @OneToMany(mappedBy = "designerProfile")
    private List<DesignerProject> designerProjects;


}
