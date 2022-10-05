package cmc.farmart.entity;

import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "A_USER_CONFIRMATION_TYPE")
@NoArgsConstructor
public class UserConfirmationType extends IdentifiableEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userConfirmationId")
    private UserConfirmation userConfirmation;

    @Column(name = "termsName")
    private String termsName;

    private boolean required; // 필수(true), 선택(false)
}
