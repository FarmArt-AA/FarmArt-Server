package cmc.farmart.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenDto {
    private String jwtAccessToken;
    private String jwtRefreshToken;
}
