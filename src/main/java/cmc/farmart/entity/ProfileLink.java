package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_PROFILE_LINK")
@Entity
public class ProfileLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long profileLinkId;

    // Farmer Profile과 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    // TODO:: Designer Profile과 연관관계
}
