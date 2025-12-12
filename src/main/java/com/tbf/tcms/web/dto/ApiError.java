package com.tbf.tcms.web.dto;

/**
 * Consistent error response body for API errors.
 */
public record ApiError(
        String timestamp,
        int status,
        String error,
        String message,
        String path
) {}
