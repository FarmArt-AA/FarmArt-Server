package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.KakaoUserInfoVo;
import cmc.farmart.domain.user.ConfirmationType;
import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import cmc.farmart.entity.UserConfirmation;
import cmc.farmart.repository.user.*;
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

    private final FarmerProfileRepository farmerProfileRepository;
    private final CropRepository cropRepository;
    private final CropImageRepository cropImageRepository;
    private final FarmProfileImageRepository farmProfileImageRepository;
    private final ProfileLinkRepository profileLinkRepository;

    private final DesignerProfileRepository designerProfileRepository;
    private final DesignerWorkAreaRepository designerWorkAreaRepository;
    private final DesignerProjectRepository designerProjectRepository;
    private final DesignerProjectImageRepository designerProjectImageRepository;

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

    public User getUserById(final Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new FarmartException(Status.ACCESS_DENIED));
    }
}
