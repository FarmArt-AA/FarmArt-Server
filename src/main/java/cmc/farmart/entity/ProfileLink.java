package cmc.farmart.entity;

import cmc.farmart.entity.designer.DesignerProfile;
import cmc.farmart.entity.farmer.FarmerProfile;
import lombok.*;

import javax.persistence.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "A_PROFILE_LINK")
@Entity
public class ProfileLink extends AuditableEntity { // 디자이너, 농부 프로필 링크

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designer_profile_id")
    private DesignerProfile designerProfile;

    @Column(name = "linkTitle")
    private String linkTitle;

    @Column(name = "webSiteUrl")
    private String webSiteUrl;

    public ProfileLink(FarmerProfile farmerProfile) {
        this.farmerProfile = farmerProfile;
    }

    public ProfileLink(DesignerProfile designerProfile) {
        this.designerProfile = designerProfile;
    }
}
