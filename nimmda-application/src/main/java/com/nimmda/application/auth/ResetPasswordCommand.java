package com.nimmda.application.auth;

public record ResetPasswordCommand(String token, String password) {
}
