package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@NoArgsConstructor
@Table(name = "A_CROP")
@Entity
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cropId")
    private Long cropId;

    // TODO:: 사용자와 연관관계를 어떻게 맺을까?
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farmer_profile_id")
    private FarmerProfile farmerProfile;

    @Column(name = "cropImage1")
    private String cropImage1;

    @Column(name = "cropImage2")
    private String cropImage2;

    @Column(name = "cropImage3")
    private String cropImage3;
}
