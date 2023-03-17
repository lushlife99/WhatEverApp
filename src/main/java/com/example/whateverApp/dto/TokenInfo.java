package com.example.whateverApp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@AllArgsConstructor
public class TokenInfo {

    private String grantType;//grantType은 JWT 대한 인증 타입으로, 여기서는 Bearer를 사용한다. 이후 HTTP 헤더에 prefix로 붙여주는 타입이기도 하다.
    private String accessToken;
    private String refreshToken;
}
