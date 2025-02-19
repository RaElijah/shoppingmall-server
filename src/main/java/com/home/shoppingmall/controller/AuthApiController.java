package com.home.shoppingmall.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import com.home.shoppingmall.config.jwt.JwtProperties;
import com.home.shoppingmall.config.jwt.JwtTokenUtil;
import com.home.shoppingmall.domain.User;
import com.home.shoppingmall.dto.UserJoinRequestDto;
import com.home.shoppingmall.dto.ResponseDto;
import com.home.shoppingmall.dto.auth.JwtRequestDto;
import com.home.shoppingmall.dto.auth.JwtResponseDto;
import com.home.shoppingmall.service.UserService;
import com.home.shoppingmall.utils.RequestUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RequiredArgsConstructor
@RestController
public class AuthApiController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenUtil jwtTokenUtil;

  private static final Logger log = LoggerFactory.getLogger(Log.class);
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/auth/signup")
  public ResponseEntity<ResponseDto> join(@Valid @RequestBody UserJoinRequestDto params) {
    log.info("### user join invoked. requestDto. username:" + params.getUsername());
    ResponseDto responseDto = ResponseDto.makeSuccessResponseStatus();
    HttpStatus responseStatus = HttpStatus.OK;
    try {
      User user = userService.signup(params);
      log.info("### user join Success, username:" + params.getUsername());
      responseDto = ResponseDto.builder()
              .msg(RequestUtil.REQUEST_SUCCESS_MSG)
              .code(RequestUtil.REQUEST_SUCCESS_CODE)
              .data(Map.of("userId", user.getId(), "username", user.getUsername(), "role", user.getRole()))
              .build();
    } catch (DataIntegrityViolationException e) {
      log.error("### user join DataIntegrityViolationException:" + e.getMessage());
      responseDto = ResponseDto.builder()
              .msg(RequestUtil.USER_SIGNUP_INFO_INVALID_MSG)
              .code(RequestUtil.USER_SIGNUP_INFO_INVALID_CODE)
              .build();
      responseStatus = HttpStatus.BAD_REQUEST;
    }
    return new ResponseEntity<>(responseDto, responseStatus);
  }

  @GetMapping("/logout")
  public String logout(HttpServletRequest request, HttpServletResponse response) {
    new SecurityContextLogoutHandler().logout(request, response,
            SecurityContextHolder.getContext().getAuthentication());
    return "redirect:/login";
  }

  @PostMapping("/auth/login")
  public ResponseEntity<ResponseDto> login(@Valid @RequestBody JwtRequestDto params) {
    log.info("### authenticate invoked. username:" + params.getUsername());
    String username = params.getUsername();
    String password = params.getPassword();

    ResponseDto responseDto;
    HttpStatus httpStatus = HttpStatus.OK;
    try {
      User user = userService.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("가입된 계정이 아닙니다"));
      if(passwordEncoder.matches(password, user.getPassword())) {
        log.info("### authenticate userinfo username:" + username);
        // username, password 를 사용해 인증토큰 생성, JwtUserDetailsService 사용
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        // 인증 및 토큰정보 생성
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        log.info("#### username = "+ userDetails.getUsername());
        String token = jwtTokenUtil.generateToken(userDetails);
        log.info("#### tokenData = "+ jwtTokenUtil.getAllClaimsFromToken(token));
        responseDto = new JwtResponseDto(
                jwtTokenUtil.getAllClaimsFromToken(token),
                JwtProperties.TOKEN_PREFIX + jwtTokenUtil.generateToken(userDetails),
                LocalDateTime.now());
      } else {
        log.error("### authenticate userinfo BadCredentialsException, username:" + username);
        responseDto = ResponseDto.builder()
                .msg(RequestUtil.USER_LOGIN_INFO_INVALID_MSG)
                .code(RequestUtil.USER_LOGIN_INFO_INVALID_CODE)
                .build();
      }
    } catch (Exception e) {
      log.error("### authenticate userinfo BadCredentialsException, username:" + username + ", Error:" + e.getMessage());
      responseDto = ResponseDto.builder()
              .msg(RequestUtil.USER_LOGIN_INFO_INVALID_MSG)
              .code(RequestUtil.USER_LOGIN_INFO_INVALID_CODE)
              .build();
      httpStatus = HttpStatus.BAD_REQUEST;
    }
    return new ResponseEntity<>(responseDto, httpStatus);
  }
}
