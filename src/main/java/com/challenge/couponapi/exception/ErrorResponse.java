package com.challenge.couponapi.exception;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

/**
 * Standardized error response structure for the API.
 */
@Getter
@Builder
public class ErrorResponse {

    private String title;
    private int status;
    private LocalDateTime timestamp;
    private String detail;
    private List<ValidationError> errors;

    @Getter
    @Builder
    public static class ValidationError {
        private String field;
        private String message;
    }
}