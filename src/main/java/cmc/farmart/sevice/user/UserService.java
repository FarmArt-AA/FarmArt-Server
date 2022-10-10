package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.KakaoUserInfoVo;
import cmc.farmart.domain.user.ConfirmationType;
import cmc.farmart.domain.user.JobTitle;
import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.ProfileLink;
import cmc.farmart.entity.User;
import cmc.farmart.entity.UserConfirmation;
import cmc.farmart.entity.designer.DesignerProfile;
import cmc.farmart.entity.designer.DesignerProject;
import cmc.farmart.entity.designer.DesignerProjectImage;
import cmc.farmart.entity.designer.DesignerWorkArea;
import cmc.farmart.entity.farmer.Crop;
import cmc.farmart.entity.farmer.CropImage;
import cmc.farmart.entity.farmer.FarmProfileImage;
import cmc.farmart.entity.farmer.FarmerProfile;
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

            // 농부, 디자이너에 따라 userProfile 생성
            if(kakaoUserInfoVo.getJobtitle().equals(JobTitle.FARMER)) {
                createFarmerProfileTable(user); // 농부 마이페이지 프로필 생성
            } else if (kakaoUserInfoVo.getJobtitle().equals(JobTitle.DESIGNER)) {
                createDesignerProfileTable(user); // 디자이너 마이페이지 프로필 생성
            } else {
                throw new FarmartException(Status.BAD_REQUEST);
            }
        } else { //이미 로그인 했던 유저라면 DB update
            updateUserBySocialData(kakaoUserInfoVo);
        }
    }

    private void createDesignerProfileTable(User user) {
        // 디자이너 프로필 생성
        DesignerProfile designerProfile = new DesignerProfile(user);
        designerProfileRepository.save(designerProfile);

        // 작업 분야 생성
        DesignerWorkArea designerWorkArea = new DesignerWorkArea(designerProfile);
        designerWorkAreaRepository.save(designerWorkArea);

        // 프로젝트 생성
        DesignerProject designerProject = new DesignerProject(designerProfile);
        designerProjectRepository.save(designerProject);

        // 프로젝트 이미지 생성
        DesignerProjectImage designerProjectImage = new DesignerProjectImage(designerProject);
        designerProjectImageRepository.save(designerProjectImage);

        // 링크 생성
        ProfileLink profileLink = new ProfileLink(designerProfile);
        profileLinkRepository.save(profileLink);
    }

    private void createFarmerProfileTable(User user) {
        // 농부 프로필 생성
        FarmerProfile farmerProfile = new FarmerProfile(user);
        farmerProfileRepository.save(farmerProfile);

        // 재배 중인 작물 생성
        Crop crop = new Crop(farmerProfile);
        cropRepository.save(crop);

        // 재배 중인 작물 이미지 생성
        CropImage cropImage = new CropImage(crop);
        cropImageRepository.save(cropImage);

        // 농장 사진 생성
        FarmProfileImage farmProfileImage = new FarmProfileImage(farmerProfile);
        farmProfileImageRepository.save(farmProfileImage);

        // 링크 생성
        ProfileLink profileLink = new ProfileLink(farmerProfile);
        profileLinkRepository.save(profileLink);
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
