package com.rewardsprogram.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        List<FieldErrorDetail> errors,
        Instant timestamp,
        String path
) {
}
