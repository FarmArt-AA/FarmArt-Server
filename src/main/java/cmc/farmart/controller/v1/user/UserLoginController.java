package cmc.farmart.controller.v1.user;

import cmc.farmart.controller.v1.user.dto.UserResponseDto;
import cmc.farmart.sevice.user.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RequestMapping("/v1/user-login")
@RequiredArgsConstructor
@RestController
public class UserLoginController {

    private final UserLoginService userLoginService;

    @Operation(summary = "카카오 로그인")
    @PostMapping("/kakao")
    public ResponseEntity<UserResponseDto> kakaoLogin(@RequestHeader("oauthToken") String accessToken, HttpServletResponse res) {
        return ResponseEntity.status(HttpStatus.OK).body(userLoginService.createToken(accessToken, res));
    }
}
