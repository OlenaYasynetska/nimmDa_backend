package com.nimmda.application.auth;

public interface VerifyEmailUseCase {

    EmailVerified verify(String token);
}
