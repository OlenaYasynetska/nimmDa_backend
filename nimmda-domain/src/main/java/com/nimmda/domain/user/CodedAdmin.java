package com.nimmda.domain.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

public final class CodedAdmin {

    public static final String ID = "nimmda-admin";
    public static final String EMAIL = "sharlot07870@gmail.com";
    public static final String PASSWORD = "NimmDaAdmin26";
    public static final String FIRST_NAME = "Admin";
    public static final String LAST_NAME = "NimmDa";

    private CodedAdmin() {
    }

    public static boolean isEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL.equals(email.trim().toLowerCase(Locale.ROOT));
    }

    public static boolean isId(String userId) {
        return ID.equals(userId);
    }

    public static boolean matches(String email, String password) {
        return isEmail(email) && passwordEquals(password);
    }

    private static boolean passwordEquals(String password) {
        if (password == null) {
            return false;
        }
        byte[] expected = PASSWORD.getBytes(StandardCharsets.UTF_8);
        byte[] actual = password.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }
}
