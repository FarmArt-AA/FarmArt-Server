package cmc.farmart.repository.user;

import cmc.farmart.entity.UserConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.module.Configuration;

public interface UserConfirmationRepository extends JpaRepository<UserConfirmation, Long> {
}
