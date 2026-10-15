package com.nimmda.application.promotion;

public class PromotionException extends RuntimeException {

    private final String code;

    public PromotionException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
