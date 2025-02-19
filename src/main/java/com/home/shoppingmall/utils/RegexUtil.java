package com.home.shoppingmall.utils;

import lombok.Getter;

@Getter
public class RegexUtil {

    public static final String EMAIL_REGEXP = "^\\w+[\\-\\w.]+@\\w+\\.\\w+";
    public static final String PHONE_REGEXP = "^(01)[0-9]\\d{4}\\d{4}";
    public static final String PASSWORD_REGEXP = "[A-Za-z0-9]{6,12}";

    static public boolean isValidEmail(String email) {

        return checkRegex(email, EMAIL_REGEXP);
    }

    static public boolean isValidPhone(String phone) {
        return checkRegex(phone, PHONE_REGEXP);
    }

    static public boolean isValidPassword(String password) {
        return checkRegex(password, PASSWORD_REGEXP);
    }

    static private boolean checkRegex(String value, String pattern) {
        return value.matches(pattern);
    }
}

