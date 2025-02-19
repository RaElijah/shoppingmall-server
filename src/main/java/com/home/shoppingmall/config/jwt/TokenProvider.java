package com.home.shoppingmall.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import com.home.shoppingmall.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {
  private final JwtProperties jwtProperties;

  public String generateToken(User user, Duration expiredAt) {
    Date now = new Date();
    return makeToken(new Date(now.getTime() + expiredAt.toMillis()), user);
  }

  private String makeToken(Date expiry, User user) {
    // jwt 토큰 생성 함수
    Date now = new Date();

    return Jwts.builder()
            .setHeaderParam(Header.TYPE, Header.JWT_TYPE) // 헤더 typ : jwt
            .setIssuer(jwtProperties.getIssuer()) // 내용 iss : propertise 파일에서 설정한 값
            .setIssuedAt(now) // 내용 iat : 현재시간
            .setExpiration(expiry) // 내용 exp : expiry 맴버 변수값
            .setSubject(user.getUsername()) // 내용 sub : 유저의 이메일
            .claim("id", user.getId()) // 클레임 id : User ID
            .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecretKey())
            .compact();
  }

  public boolean validToken(String token) {
    // jwt 토큰 유효성 검증
    try {
      Jwts.parser()
              .setSigningKey(jwtProperties.getSecretKey())
              .parseClaimsJws(token);

      return true;
    } catch (Exception e) {
      return false;
    }
  }


  public Authentication getAuthentication(String token) {
    // 토큰 기반으로 인증 정보 가져오는 함수
    Claims claims = getClaims(token);
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

    return new UsernamePasswordAuthenticationToken(new org.springframework.security.core.userdetails.User(claims.getSubject
            (), "", authorities), token, authorities);
  }

  public Long getUserId(String token) {
    // 토큰 기반으로 유저 ID 가져오는 함수
    Claims claims = getClaims(token);
    return claims.get("id", Long.class);
  }

  private Claims getClaims(String token) { // 클레임 조회
    return Jwts.parser()
            .setSigningKey(jwtProperties.getSecretKey())
            .parseClaimsJws(token)
            .getBody();
  }
}
