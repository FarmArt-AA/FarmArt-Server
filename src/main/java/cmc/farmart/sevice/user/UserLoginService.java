package cmc.farmart.sevice.user;

import cmc.farmart.common.exception.FarmartException;
import cmc.farmart.common.exception.Status;
import cmc.farmart.controller.v1.user.dto.KakaoLoginDto;
import cmc.farmart.controller.v1.user.dto.KakaoLoginSignUpDto;
import cmc.farmart.controller.v1.user.dto.KakaoUserInfoVo;
import cmc.farmart.domain.user.ConfirmationType;
import cmc.farmart.domain.user.JobTitle;
import cmc.farmart.domain.user.SocialType;
import cmc.farmart.entity.User;
import cmc.farmart.jwt.JwtUtil;
import cmc.farmart.jwt.dto.TokenDto;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserLoginService {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public KakaoLoginDto signUp(final String accessToken, final KakaoLoginSignUpDto.Reqeust reqeust, final HttpServletResponse res) {

        // 필수 약관 동의 validation
        verifyConfirmation(reqeust.getConfirmationTypes());

        //AccessToken으로 KakaoUserInfo 받기
        KakaoUserInfoVo kakaoUserInfoVo = getKakaoUserInfo(accessToken);

        // 사용자 존재 여부 체크
        Assert.notNull(kakaoUserInfoVo.getSocialId(), "유저 정보가 존재하지 않습니다..");

        // Auth로 받아온 사용자 정보를 jwt에 담는다.
        TokenDto tokens = jwtUtil.createToken(kakaoUserInfoVo);
        kakaoUserInfoVo.setRefreshToken(tokens.getJwtRefreshToken());

        // 사용자 이름, 닉네임 저장
        kakaoUserInfoVo.setUserName(reqeust.getUserName());
        kakaoUserInfoVo.setUserNickName(reqeust.getUserNickName());
        kakaoUserInfoVo.setJobtitle(JobTitle.valueOf(reqeust.getJobTitle()));
        kakaoUserInfoVo.setPhoneNumber(reqeust.getPhoneNumber());

        //socialId 기준으로 DB select하여 User 데이터가 없으면 Insert, 있으면 Update
        userService.insertOrUpdateUser(kakaoUserInfoVo);

        Optional<User> userByKakaoSocialData = userService.findUserBySocialData(kakaoUserInfoVo.getSocialId(), kakaoUserInfoVo.getSocialType());

        // UserResponseDto에 userId 추가
        KakaoLoginDto kakaoLoginDto = new KakaoLoginDto(userByKakaoSocialData.get().getId(), kakaoUserInfoVo.getEmail(), kakaoUserInfoVo.getProfileImageUrl());

        res.addHeader("at-jwt-access-token", tokens.getJwtAccessToken());
        res.addHeader("at-jwt-refresh-token", tokens.getJwtRefreshToken());

        return kakaoLoginDto;

    }

    private void verifyConfirmation(final Set<ConfirmationType> userConfirmationTypes) {
        if (Boolean.FALSE.equals(Arrays.stream(ConfirmationType.values())
                .filter(confirmation -> confirmation.required().equals(Boolean.TRUE))
                .allMatch(type -> userConfirmationTypes.stream().anyMatch(userConfirmationType -> userConfirmationType.equals(type))))) {
            throw new FarmartException(Status.CONFIRMATION_REQUIRED_TERMS);
        }

    }

    private KakaoUserInfoVo getKakaoUserInfo(String accessToken) {

        //UserRequestDto에 정보 받기
        KakaoUserInfoVo kakaoUserInfoVo = new KakaoUserInfoVo();

        try {
            URL url = new URL("https://kapi.kakao.com/v2/user/me");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            //    요청에 필요한 Header에 포함될 내용
            conn.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = conn.getResponseCode();
            System.out.println("responseCode : " + responseCode);

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));


            String line = "";
            String result = "";

            while ((line = br.readLine()) != null) {
                result += line;
            }
            System.out.println("response body : " + result);

            JsonElement element = JsonParser.parseString(result);

            String kakaoId = element.getAsJsonObject().get("id").getAsString();
            JsonObject properties = element.getAsJsonObject().get("properties").getAsJsonObject();
            JsonObject kakao_account = element.getAsJsonObject().get("kakao_account").getAsJsonObject();

            String profileImageUrl = properties.getAsJsonObject().get("profile_image").getAsString();
            String email = kakao_account.getAsJsonObject().get("email").getAsString();

            //    UserRequestDto에 값 주입
            kakaoUserInfoVo.setSocialId(kakaoId);
            kakaoUserInfoVo.setEmail(email);
            kakaoUserInfoVo.setProfileImageUrl(profileImageUrl);
            kakaoUserInfoVo.setSocialType(SocialType.KAKAO);


        } catch (IOException e) {   // 잘못된 값 주입하고 에러 터지는 지 Test
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;    //글로벌 에러 처리할 때 변경
        }

        return kakaoUserInfoVo;
    }
}
