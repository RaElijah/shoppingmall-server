package com.home.shoppingmall.dto.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * login 시 사용하는 request filed 객체
 */
@Getter
@NoArgsConstructor
public class JwtRequestDto implements Serializable {
    private String username;
    private String password;

    public JwtRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}