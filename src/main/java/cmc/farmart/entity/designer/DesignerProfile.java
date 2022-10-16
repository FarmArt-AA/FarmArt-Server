package cmc.farmart.entity.designer;

import cmc.farmart.entity.AuditableEntity;
import cmc.farmart.entity.ProfileLink;
import cmc.farmart.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROFILE")
@Entity
public class DesignerProfile extends AuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_user_id")
    private User user;

    @Column(name = "designer_profile_image_path")
    private String designerProfileImagePath; // 디자이너 프로필 이미지

    @Column(name = "designer_profile_title")
    private String designerProfileTitle; // 프로필 제목

    @Column(name = "designer_profile_profile_introduce")
    private String designerProfileIntroduce; // 소개

    @OneToMany(mappedBy = "designerWorkAreaType")
    private List<DesignerWorkArea> designerWorkAreaTypes; // 작업분야

    @OneToMany(mappedBy = "designerProfile")
    private List<DesignerProject> designerProjects; // 디자이너 프로젝트

    @OneToMany(mappedBy = "designerProfile")
    private List<ProfileLink> profileLinks; // 링크

    public DesignerProfile(User user) {
        this.user = user;
    }

    public void setDesignerProfile(String designerProfileTitle, String designerProfileIntroduce) {
        this.designerProfileTitle = designerProfileTitle;
        this.designerProfileIntroduce = designerProfileIntroduce;
    }
}
