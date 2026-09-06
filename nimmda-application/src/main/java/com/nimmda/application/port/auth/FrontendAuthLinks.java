package com.nimmda.application.port.auth;

public interface FrontendAuthLinks {

    String verifyUrl(String token);

    String resetUrl(String token);
}
