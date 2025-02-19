package com.home.shoppingmall.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.home.shoppingmall.utils.RequestUtil;

import java.util.Map;

@Getter
@NoArgsConstructor
public class ResponseDto {
    private String msg = RequestUtil.REQUEST_SUCCESS_MSG;
    private Integer code = RequestUtil.REQUEST_SUCCESS_CODE;
    private Map<String, Object> data;

    @Builder
    public ResponseDto(String msg, Integer code, Map<String, Object> data) {
        this.msg = msg;
        this.code = code;
        this.data = data;
    }

    public static ResponseDto makeSuccessResponseStatus() {
        return ResponseDto.builder()
                .msg(RequestUtil.REQUEST_SUCCESS_MSG)
                .code(RequestUtil.REQUEST_SUCCESS_CODE)
                .build();
    }

    public static ResponseDto makeFailResponseStatus() {
        return ResponseDto.builder()
                .msg(RequestUtil.REQUEST_FAIL_MSG)
                .code(RequestUtil.REQUEST_FAIL_CODE)
                .build();
    }
}
