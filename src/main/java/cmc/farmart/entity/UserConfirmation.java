package cmc.farmart.entity;

import cmc.farmart.domain.user.ConfirmationType;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "A_USER_CONFIRMATION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfirmation extends AuditableEntity{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_user_id")
    User user;

    @Column(name = "user_confirmation_type")
    @Enumerated(EnumType.STRING)
    ConfirmationType confirmationType;

    private String ipAddress;

}
