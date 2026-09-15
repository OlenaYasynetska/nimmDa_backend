package com.nimmda.web.error;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String code,
        List<String> suggestions
) {
    public ApiErrorResponse {
        suggestions = suggestions == null ? List.of() : List.copyOf(suggestions);
    }

    public ApiErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            String code
    ) {
        this(timestamp, status, error, message, path, code, List.of());
    }
}
