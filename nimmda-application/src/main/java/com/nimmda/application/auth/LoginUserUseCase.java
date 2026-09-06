package com.nimmda.application.auth;

public interface LoginUserUseCase {

    AuthSession login(LoginUserCommand command);
}
