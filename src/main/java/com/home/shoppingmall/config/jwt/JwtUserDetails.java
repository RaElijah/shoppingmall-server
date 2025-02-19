package com.home.shoppingmall.config.jwt;

import lombok.Getter;
import com.home.shoppingmall.domain.User;
import com.home.shoppingmall.domain.Role;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 만료기한 상관 없이 userdetail 객체를 생성
 * oauth, jwtFilter, passwordExpired 시에 사용
 */
@Getter
public class JwtUserDetails extends org.springframework.security.core.userdetails.User {

    private User user;

    public JwtUserDetails(Long id, String username, String password, Role role, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.user = new User(id, username, password, role);
    }
}
