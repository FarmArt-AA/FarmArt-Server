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
    @JoinColumn(name = "userId")
    User user;

    @Column(name = "user_confirmation_type_id")
    @Enumerated(EnumType.ORDINAL) // 약관 동의는 추가 변경할 경우가 적기 때문에 ordinal을 선택.
    ConfirmationType confirmationType;

    private String ipAddress;

    @OneToMany(mappedBy = "confirmationType")
    List<UserConfirmationType> typeList;

}
