package com.nimmda.application.auth;

public interface ConfirmEmailUseCase {

    void confirm(String token);
}
