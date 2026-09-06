package com.nimmda.application.auth;

public class AuthException extends RuntimeException {

    private final String code;

    public AuthException(String code) {
        super(code);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
