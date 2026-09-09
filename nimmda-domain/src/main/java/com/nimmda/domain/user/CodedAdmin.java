package com.nimmda.domain.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public final class CodedAdmin {

    public static final String ID = "nimmda-admin";
    public static final String FIRST_NAME = "Super";
    public static final String LAST_NAME = "Admin";

    private CodedAdmin() {
    }

    public static boolean isId(String userId) {
        return ID.equals(userId);
    }

    public static boolean isEmail(String email, String configuredEmail) {
        if (email == null || configuredEmail == null || configuredEmail.isBlank()) {
            return false;
        }
        return configuredEmail.trim().toLowerCase(Locale.ROOT).equals(email.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean matches(String email, String password, String configuredEmail, String configuredPassword) {
        if (configuredPassword == null || configuredPassword.isBlank()) {
            return false;
        }
        if (!isEmail(email, configuredEmail) || password == null) {
            return false;
        }
        byte[] expected = configuredPassword.getBytes(StandardCharsets.UTF_8);
        byte[] actual = password.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
