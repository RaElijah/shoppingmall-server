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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
  private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

  private final TokenProvider tokenProvider;
  private final UserDetailService userService;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
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
    return http
            .httpBasic().and().csrf().disable()// token을 사용하는 방식이기 때문에 csrf를 disable합니다.
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) // jwt token으로 인증할것이므로 세션필요없으므로 생성안함.
            .and()
            .authorizeHttpRequests()
            .requestMatchers(
                    "/css/**",
                    "/images/**",
                    "/js/**",
                    "/resources/**",
                    "/auth/**",
                    "/h2-console/**"
            ).permitAll()
            .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
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

  @Bean
  public AuthenticationManager authenticationManager(
          HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
          UserDetailService userDetailService
  ) throws Exception {
    return http.getSharedObject(AuthenticationManagerBuilder.class)
            .userDetailsService(userService)
            .passwordEncoder(bCryptPasswordEncoder)
            .and()
            .build();
  }
}
