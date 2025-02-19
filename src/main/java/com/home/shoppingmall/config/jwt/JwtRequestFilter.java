package com.home.shoppingmall.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.home.shoppingmall.domain.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

// @Component 로 filter 를 빈으로 등록하면 servlet filter 에 자동 등록고
// 스프링 시큐리티에서도 설정하기에 중복 등록되기에 사용하면 안된다.
// servlet filter 는 시큐리티의 ignore 설정도 적용되지 않음으로 servlet filter 는 등록지 않을 필요가 있다.
// SecurityConfig 의 FilterRegistrationBean 참고
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
// OncePerRequestFilter 하나의 request 당 필터가 한번만 실행되는 것을 보장

  private final static String DEFAULT_PASSWORD = "default_password";

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

    // headaer 로부터 토큰 흭득
    final String requestTokenHeader = request.getHeader("Authorization");
    // JWT Token is in the form "Bearer token". Remove Bearer word and get
    // only the Token
    if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
      String jwtToken = requestTokenHeader.substring(7);
      String username = jwtTokenUtil.getUsernameFromToken(jwtToken);
      //if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      if (username != null) {
        // UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(username);
        // 토큰 유효시간도 많고 Spring Security 부터 가져온 userDetails 과도 인증저보가 일치하면
        //if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
        if (!jwtTokenUtil.isTokenExpired(jwtToken)) {
                    /*
                    rest api 의 경우 session 을 사용하지 않기에 SecurityContextHolder 에 인증정보가 저장되어 있지 않아
                    항상 db io 가 발생하기에 별도의 방법으로 authentication(인증객체) 를 가져와야 한다.
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    */
          Authentication authentication = getAuthentication(jwtToken);
          // 세션을 사용하지 않음에도 SecurityContextHolder 를 사용할 수 있는건 해당스레드 안에서는 인증정보를 유지하기 위해
          SecurityContextHolder.getContext().setAuthentication(authentication);
          logger.debug("JwtRequestFilter - username:" + username + ", url:" + request.getMethod() + ":" + request.getRequestURL());
        } else {
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token has expired");
          return;
        }
      } else {
        // jwt token 에서 subject 를 찾을 수 없을경우
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token does not have username");
        return;
      }
    } else {
      // header 에 jwt 토큰이 없을경우
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token does not begin with Bearer String");
      return;
    }
    chain.doFilter(request, response);
  }

  /**
   * 패스워드를 사용하지 않고 인증객체를 만들기 위해 별도의 메서드를 생성, username, role 만을 가지고 인증객체를 만든다.
   */
  public Authentication getAuthentication(String token) {
    Map<String, Object> parseInfo = jwtTokenUtil.getUserParseInfo(token);
    Long userId = (Long) parseInfo.get("id");
    String username = (String) parseInfo.get("username");
    String role = (String) parseInfo.get("role");
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(role));

    //인증객체
    JwtUserDetails jwtUserDetail = new JwtUserDetails(
            userId,
            username,
            DEFAULT_PASSWORD,
            Role.getValue(role),
            authorities);

    // 비밀번호를 security context 까지 가져갈 필요는 없음으로 credential 은 null 처리
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(jwtUserDetail, null, jwtUserDetail.getAuthorities());
    return usernamePasswordAuthenticationToken;
  }
}