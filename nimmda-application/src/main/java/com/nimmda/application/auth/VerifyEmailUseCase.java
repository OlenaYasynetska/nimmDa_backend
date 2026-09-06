package com.nimmda.application.auth;

public interface VerifyEmailUseCase {

    AuthSession verify(String token);
}
