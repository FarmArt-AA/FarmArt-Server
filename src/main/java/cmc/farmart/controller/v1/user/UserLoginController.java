package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.KakaoLoginDto;
import cmc.farmart.controller.v1.user.dto.KakaoLoginSignUpDto;
import cmc.farmart.sevice.user.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RequestMapping("/v1/user-login")
@RequiredArgsConstructor
@RestController
public class UserLoginController {

    private final UserLoginService userLoginService;

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao")
    public ResponseEntity<KakaoLoginDto> kakaoLogin(
            @Parameter(description = "kakaoAccessToken") @RequestHeader("oauthToken") String accessToken,
            @RequestBody KakaoLoginSignUpDto.Reqeust kakaoLoginSignUpDto,
            HttpServletResponse res) {
        return ResponseEntity.status(HttpStatus.OK).body(userLoginService.createToken(accessToken,kakaoLoginSignUpDto, res));
    }
}
