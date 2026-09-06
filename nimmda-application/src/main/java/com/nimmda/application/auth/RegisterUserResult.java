package com.nimmda.application.auth;

public record RegisterUserResult(boolean mailSent, String verifyUrl) {
}
