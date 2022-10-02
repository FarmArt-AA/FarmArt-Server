package cmc.farmart.sevice.user;

import cmc.farmart.controller.v1.user.dto.KakaoUserInfoDto;
import cmc.farmart.domain.user.SocialType;

import cmc.farmart.entity.User;
import cmc.farmart.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void insertOrUpdateUser(KakaoUserInfoDto kakaoUserInfoDto) {
        String socialId = kakaoUserInfoDto.getSocialId();
        SocialType socialType = kakaoUserInfoDto.getSocialType();
        //처음 로그인 하는 유저면 DB에 insert
        if (Boolean.FALSE.equals(findUserBySocialData(socialId, socialType).isPresent())) {
            User user = kakaoUserInfoDto.toEntity(); //기본 Role = ROLE.USER
            userRepository.save(user);
        } else { //이미 로그인 했던 유저라면 DB update
            updateUserBySocialData(kakaoUserInfoDto);
        }
    }


    public Optional<User> findUserBySocialData(String socialId, SocialType socialType) {
        Optional<User> user = userRepository.findBySocialIdAndSocialType(socialId, socialType);
        return user;
    }

    public void updateUserBySocialData(KakaoUserInfoDto kakaoUserInfoDto) {
        userRepository.updateUserBySocialIdAndSocialType(kakaoUserInfoDto.getEmail(), kakaoUserInfoDto.getProfileImageUrl(), kakaoUserInfoDto.getRefreshToken(), kakaoUserInfoDto.getSocialId(), kakaoUserInfoDto.getSocialType());
    }
}
