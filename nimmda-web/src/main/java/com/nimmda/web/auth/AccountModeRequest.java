package com.nimmda.web.auth;

import jakarta.validation.constraints.NotBlank;

public record AccountModeRequest(@NotBlank String role) {
}
