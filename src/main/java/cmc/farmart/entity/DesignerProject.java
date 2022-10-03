package cmc.farmart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Table(name = "A_DESIGNER_PROFILE")
@Entity
public class DesignerProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "designerProjectId")
    private Long designerProjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designerProfileId")
    private DesignerProfile designerProfile;

    @Column(name = "designerProjectTitle")
    private String designerProjectTitle;

    @Column(name = "designerProjectStartDate")
    private LocalDateTime designerProjectStartDate;

    @Column(name = "designerProjectEndDate")
    private LocalDateTime designerProjectEndDate;

    @Column(name = "designerProjectIntroduce")
    private String designerProjectIntroduce;

    @Column(name = "designerProjectImageUrl1")
    private String designerProjectImageUrl1;

    @Column(name = "designerProjectImageUrl2")
    private String designerProjectImageUrl2;

    @Column(name = "designerProjectImageUrl3")
    private String designerProjectImageUrl3;
}
