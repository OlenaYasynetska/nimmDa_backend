package com.nimmda.application.auth;

public interface UpdateAccountModeUseCase {

    AuthSession updateMode(String userId, String accountMode);
}
