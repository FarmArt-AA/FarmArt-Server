package cmc.farmart.sevice.user;

import cmc.farmart.controller.v1.user.dto.UserInfoDto;
import cmc.farmart.controller.v1.user.dto.UserResponseDto;
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
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class UserLoginService {

    private final JwtUtil jwtUtil;
    private final UserService userService;

    public UserResponseDto createToken(String accessToken, HttpServletResponse res) {

        //AccessToken으로 UserInfo 받기
        UserInfoDto userInfo = getUserInfo(accessToken);

        Assert.notNull(userInfo.getSocialId(), "유저 정보가 존재합니다.");

        TokenDto tokens = jwtUtil.createToken(userInfo);
        userInfo.setRefreshToken(tokens.getJwtRefreshToken());

        //socialId 기준으로 DB select하여 User 데이터가 없으면 Insert, 있으면 Update
        userService.insertOrUpdateUser(userInfo);

        Optional<User> userBySocialData = userService.findUserBySocialData(userInfo.getSocialId(), userInfo.getSocialType());

        //UserResponseDto에 userId 추가
        UserResponseDto userResponseDto = new UserResponseDto(userBySocialData.get().getUserId(), userInfo.getUsername(), userInfo.getEmail(), userInfo.getImgURL());

        res.addHeader("at-jwt-access-token", tokens.getJwtAccessToken());
        res.addHeader("at-jwt-refresh-token", tokens.getJwtRefreshToken());

        return userResponseDto;

    }

    private UserInfoDto getUserInfo(String accessToken) {

        //UserRequestDto에 정보 받기
        UserInfoDto userInfo = new UserInfoDto();

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

            String nickname = properties.getAsJsonObject().get("nickname").getAsString();
            String imgURL = properties.getAsJsonObject().get("profile_image").getAsString();
            String email = kakao_account.getAsJsonObject().get("email").getAsString();

            //    UserRequestDto에 값 주입
            System.out.println("nickname = " + nickname);
            userInfo.setUsername(nickname);
            userInfo.setSocialId(kakaoId);
            userInfo.setEmail(email);
            userInfo.setImgURL(imgURL);
            userInfo.setSocialType(SocialType.KAKAO);


        } catch (IOException e) {   // 잘못된 값 주입하고 에러 터지는 지 Test
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;    //글로벌 에러 처리할 때 변경
        }

        return userInfo;
    }
}
