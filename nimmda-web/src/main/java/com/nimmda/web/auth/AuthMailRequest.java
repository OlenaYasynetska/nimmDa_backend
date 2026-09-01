package com.nimmda.web.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthMailRequest(
        @NotBlank @Email String to,
        @NotBlank String type,
        @NotBlank String link
) {
}
