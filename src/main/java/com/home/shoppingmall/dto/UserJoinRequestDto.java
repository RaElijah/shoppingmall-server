package com.home.shoppingmall.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import jakarta.validation.constraints.Pattern;
import com.home.shoppingmall.domain.User;
import com.home.shoppingmall.domain.Role;
import com.home.shoppingmall.utils.RegexUtil;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserJoinRequestDto {
    @NotBlank(message = "이메일 입력 필수")
    @Pattern(regexp = RegexUtil.EMAIL_REGEXP, message = "유효하지 않은 이메일입니다")
    private String username;

    @NotBlank(message = "비밀번호 입력 필수")
    @Pattern(regexp = RegexUtil.PASSWORD_REGEXP, message = "유효하지 않은 비밀번호입니다")
    private String password;

    @NotBlank(message = "권한 입력 필수")
    private String role; //ADMIN, USER, GUEST

    public User toEntity(String encryptPassword) {
        return User.builder()
                .username(this.username)
                .password(encryptPassword)
                .role(Role.valueOf(role))
                .build();
    }

    public void checkJoinUser(String secret) {
        if (role.equals(Role.ADMIN.name())) {
            return;
        }
    }
}