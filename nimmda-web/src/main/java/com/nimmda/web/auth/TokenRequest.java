package com.nimmda.web.auth;

import jakarta.validation.constraints.NotBlank;

public record TokenRequest(@NotBlank String token) {
}
