package com.nimmda.application.auth;

public interface RequestPasswordResetUseCase {

    RegisterUserResult requestReset(String email);
}
