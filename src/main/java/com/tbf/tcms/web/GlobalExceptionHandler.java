package com.tbf.tcms.web;

import com.tbf.tcms.web.dto.ApiError;
import com.tbf.tcms.web.error.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 404 - Resource not found (custom or JPA/Java variants)
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, messageOrDefault(ex, "Resource not found"), request);
    }

    // 400 - Validation: @Valid on request bodies
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "Validation failed";
        }
        ApiError error = new ApiError(Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                request.getDescription(false).replace("uri=", ""));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 400 - Validation: @Validated on query/path params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.joining(", "));
        if (message.isBlank()) {
            message = "Constraint violation";
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - Data integrity issues
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String friendly = "Request violates data integrity constraints";
        ex.getMostSpecificCause();
        String detail = ex.getMostSpecificCause().getMessage();
        String message = friendly + (detail != null ? ": " + detail : "");
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - Common bad request scenarios
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, BindException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, messageOrDefault(ex, "Bad request"), request);
    }

    // 500 - Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest request) {
        // If Spring Security is present and this is AccessDeniedException, map to 403
        if (isAccessDenied(ex)) {
            return build(HttpStatus.FORBIDDEN, "Access is denied", request);
        }
        String message = "An unexpected error occurred"; // generic for production
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request) {
        ApiError body = new ApiError(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(body);
    }

    private String formatViolation(ConstraintViolation<?> v) {
        String path = v.getPropertyPath() != null ? v.getPropertyPath().toString() : "";
        String msg = v.getMessage();
        if (path == null || path.isBlank()) return msg;
        return path + ": " + msg;
    }

    private String messageOrDefault(Exception ex, String fallback) {
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? fallback : msg;
    }

    private boolean isAccessDenied(Exception ex) {
        // Avoid compile-time dependency on Spring Security
        Throwable current = ex;
        while (current != null) {
            if (current.getClass().getName().equals("org.springframework.security.access.AccessDeniedException")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
