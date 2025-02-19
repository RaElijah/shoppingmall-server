package com.home.shoppingmall.config;

import lombok.RequiredArgsConstructor;
import com.home.shoppingmall.config.jwt.JwtAccessDeniedHandler;
import com.home.shoppingmall.config.jwt.JwtAuthenticationEntryPoint;
import com.home.shoppingmall.config.jwt.JwtRequestFilter;
import com.home.shoppingmall.config.jwt.TokenProvider;
import com.home.shoppingmall.service.UserDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  @Autowired
  private JwtRequestFilter jwtRequestFilter;

//  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  private final TokenProvider tokenProvider;
  private final UserDetailService userService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  /**
   * @Component 로 등록된 JwtRequestFilter 가 일반 서블릿 필터로 중복 등록되는것을 방지
   */
  @Bean
  public FilterRegistrationBean registration(JwtRequestFilter filter) {
    FilterRegistrationBean registration = new FilterRegistrationBean(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  public WebSecurityCustomizer configure() {
    return (web) -> web.ignoring()
            .requestMatchers(toH2Console())
            .requestMatchers("/auth/**")
            .requestMatchers("/static/**")
            .requestMatchers("/favicon.ico");
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .sessionManagement(sessionManagement -> // 세션 관리 설정
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 세션을 사용하지 않음
            )
            .authorizeHttpRequests(authorizeRequests -> // HTTP 요청에 대한 보안 규칙을 정의
                    authorizeRequests
                            .requestMatchers(
                                    new AntPathRequestMatcher("/css/**"),
                                    new AntPathRequestMatcher("/images/**"),
                                    new AntPathRequestMatcher("/js/**"),
                                    new AntPathRequestMatcher("/resources/**"),
                                    new AntPathRequestMatcher("/auth/**"),
                                    new AntPathRequestMatcher("/h2-console/**")
                            ).permitAll() // 경로에 대한 접근을 허용
                            .anyRequest().authenticated() // 그 외의 모든 요청은 인증을 요구
            )
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class) // UsernamePasswordAuthenticationFilter 앞에 jwtRequestFilter 추가
            .csrf(csrf -> csrf.disable());
    return http.build(); // HTTP 보안 설정을 빌드하여 반환
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://localhost:8080")); //URLs you want to allow
    configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH")); //methods you want to allow
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

//  @Bean
//  public AuthenticationManager authenticationManager(AuthenticationManagerBuilder builder) throws Exception {
//    builder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder());
//    return builder.build();
//  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
