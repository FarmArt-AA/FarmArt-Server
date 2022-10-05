package cmc.farmart.entity;

import cmc.farmart.domain.user.ConfirmationType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "A_USER_CONFIRMATION_TYPE")
@NoArgsConstructor
public class UserConfirmationType extends IdentifiableEntity {

    @Column(name = "termsName")
    @Enumerated(EnumType.STRING)
    private ConfirmationType confirmationType; // 약관 동의 내용

    @Column(name = "required")
    private boolean required; // 필수(true), 선택(false)
}
