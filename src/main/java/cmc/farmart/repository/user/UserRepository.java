package cmc.farmart.repository.user;


import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

@SuppressWarnings("unchecked")  //검증되지 않은 연산자 관련 경고를 무시
public interface UserRepository extends JpaRepository<User, Long> {

    User save(User user);

    List<User> findAll();

    Optional<User> findByUserId(Long userId);

    Optional<User> findBySocialIdAndSocialType(String socialId, SocialType socialType);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.username = ?1, u.email = ?2, u.imgUrl = ?3, u.refreshToken = ?4 WHERE u.socialId = ?5 AND u.socialType = ?6")
    void updateUserBySocialIdAndSocialType(String username, String email, String imgURL, String refreshToken, String socialId, SocialType socialType);

    @Query("SELECT u.refreshToken FROM User u WHERE u.socialId = ?1 AND u.socialType = ?2")
    String findRefreshTokenBySocialIdAndSocialType(String socialId, SocialType socialType);

//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE User u SET u.deviceToken = ?1 WHERE u.userId = ?2")
//    void updateDeviceTokenByUserId(String deviceToken, Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.refreshToken = ?1 WHERE u.socialId = ?2 AND u.socialType = ?3")
    void updateRefreshTokenBySocialIdAndSocialType(String refreshToken, String socialId, SocialType socialType);

    void delete(User user);

}

