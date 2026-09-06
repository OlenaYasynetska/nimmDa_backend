package com.nimmda.application.auth;

public interface ResendVerificationUseCase {

    RegisterUserResult resend(String email);
}
