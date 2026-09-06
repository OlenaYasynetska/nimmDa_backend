package com.nimmda.application.auth;

public record LoginUserCommand(String email, String password, String accountMode) {
}
