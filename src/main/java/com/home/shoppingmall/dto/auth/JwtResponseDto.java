package com.home.shoppingmall.dto.auth;

import io.jsonwebtoken.Claims;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.home.shoppingmall.dto.ResponseDto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@NoArgsConstructor
public class JwtResponseDto extends ResponseDto implements Serializable {
    private LocalDateTime loginTime;
    private Map<String, Object> data;

    public JwtResponseDto(Claims tokenData, String token, LocalDateTime loginTime) {
        super();
        this.data = makeData(tokenData, token);
        this.loginTime = loginTime;
    }

    private Map<String, Object> makeData(Claims tokenData, String token) {
        Map<String, Object> result = new HashMap<>();
        result.put("tokenData", tokenData);
        result.put("token", token);
        return result;
    }
}