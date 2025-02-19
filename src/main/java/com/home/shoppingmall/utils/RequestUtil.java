package com.home.shoppingmall.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestUtil {
  public static final String REQUEST_SUCCESS_MSG = "요청 성공";
  public static final Integer REQUEST_SUCCESS_CODE = 0;

  public static final String REQUEST_FAIL_MSG = "요청 실패";
  public static final Integer REQUEST_FAIL_CODE = 1;

  public static final String USER_LOGIN_INFO_INVALID_MSG = "로그인 실패";
  public static final Integer USER_LOGIN_INFO_INVALID_CODE = 1001;

  public static final String USER_SIGNUP_INFO_INVALID_MSG = "회원가입 실패";
  public static final Integer USER_SIGNUP_INFO_INVALID_CODE = 1002;
}
