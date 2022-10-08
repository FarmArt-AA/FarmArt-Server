package cmc.farmart.sevice.user;

import cmc.farmart.controller.v1.user.dto.KakaoUserInfoVo;
import cmc.farmart.domain.user.ConfirmationType;
import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import cmc.farmart.entity.UserConfirmation;
import cmc.farmart.repository.user.UserConfirmationRepository;
import cmc.farmart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConfirmationRepository userConfirmationRepository;

    public void insertOrUpdateUser(final Set<ConfirmationType> confirmationTypes, final String ipAddress, KakaoUserInfoVo kakaoUserInfoVo) {
        String socialId = kakaoUserInfoVo.getSocialId();
        SocialType socialType = kakaoUserInfoVo.getSocialType();

        //처음 로그인 하는 유저면 DB에 insert(회원 가입)
        User user = kakaoUserInfoVo.toEntity();
        if (Boolean.FALSE.equals(findUserBySocialData(socialId, socialType).isPresent())) {
            userRepository.save(user); // 사용자 회원 가입
            insertConfirmation(confirmationTypes, user, ipAddress); // 약관 동의 저장
        } else { //이미 로그인 했던 유저라면 DB update
            updateUserBySocialData(kakaoUserInfoVo);
        }
    }

    public void insertConfirmation(final Set<ConfirmationType> confirmationTypes, final User user, final String ipAddress) {
        confirmationTypes.forEach(confirmationType -> {
            UserConfirmation userConfirmation = UserConfirmation.builder()
                    .user(user)
                    .confirmationType(confirmationType)
                    .ipAddress(ipAddress)
                    .build();
            userConfirmationRepository.save(userConfirmation);
        });
    }

    public Optional<User> findUserBySocialData(String socialId, SocialType socialType) {
        Optional<User> user = userRepository.findBySocialIdAndSocialType(socialId, socialType);
        return user;
    }

    public void updateUserBySocialData(KakaoUserInfoVo kakaoUserInfoVo) {
        userRepository.updateUserBySocialIdAndSocialType(kakaoUserInfoVo.getEmail(), kakaoUserInfoVo.getProfileImageUrl(), kakaoUserInfoVo.getRefreshToken(), kakaoUserInfoVo.getSocialId(), kakaoUserInfoVo.getSocialType());
    }
}
