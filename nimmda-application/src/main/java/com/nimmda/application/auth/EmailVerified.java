package com.nimmda.application.auth;

public record EmailVerified(boolean verified, String message, String email) {
}
