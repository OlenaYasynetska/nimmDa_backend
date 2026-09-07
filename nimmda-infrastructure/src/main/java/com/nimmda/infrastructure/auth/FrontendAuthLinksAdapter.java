package com.nimmda.infrastructure.auth;

import com.nimmda.application.port.auth.FrontendAuthLinks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FrontendAuthLinksAdapter implements FrontendAuthLinks {

    private final String baseUrl;

    public FrontendAuthLinksAdapter(@Value("${app.frontend.base-url:http://localhost:4200}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    @Override
    public String verifyUrl(String token) {
        return baseUrl + "/auth/verify-email?token=" + token;
    }

    @Override
    public String resetUrl(String token) {
        return baseUrl + "/auth/reset-password?token=" + token;
    }
}
