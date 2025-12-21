package com.tbf.tcms.web;

import com.tbf.tcms.web.dto.ApiError;
import com.tbf.tcms.web.error.ResourceNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // 404 - Resource not found (custom or JPA/Java variants)
    @ExceptionHandler({ResourceNotFoundException.class, EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
        log.warn("Resource not found at path {} - {}", request.getRequestURI(), safeMessage(ex));
        return build(HttpStatus.NOT_FOUND, messageOrDefault(ex, "Resource not found"), request);
    }

    // 400 - Validation: @Valid on request bodies
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fe -> fe.getField(),
                        fe -> fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage(),
                        (msg1, msg2) -> msg1 // keep first message if duplicate field errors
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
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
        log.warn("Constraint violation at path {} - {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - Data integrity issues
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String friendly = "Request violates data integrity constraints";
        ex.getMostSpecificCause();
        String detail = ex.getMostSpecificCause().getMessage();
        String message = friendly + (detail != null ? ": " + detail : "");
        log.warn("Data integrity violation at path {} - {}", request.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    // 400 - Common bad request scenarios
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, BindException.class})
    public ResponseEntity<ApiError> handleBadRequest(Exception ex, HttpServletRequest request) {
        log.warn("Bad request at path {} - {}", request.getRequestURI(), safeMessage(ex));
        return build(HttpStatus.BAD_REQUEST, messageOrDefault(ex, "Bad request"), request);
    }

    // 500 - Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(Exception ex, HttpServletRequest request) {
        // If Spring Security is present and this is AccessDeniedException, map to 403
        if (isAccessDenied(ex)) {
            log.warn("Access denied at path {} - {}", request.getRequestURI(), safeMessage(ex));
            return build(HttpStatus.FORBIDDEN, "Access is denied", request);
        }
        String message = "An unexpected error occurred"; // generic for production
        log.error("Unhandled exception at path {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    // 403 - Access denied (Spring Security)
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                                       HttpServletRequest request) {
        log.warn("Access denied at path {} - {}", request.getRequestURI(), safeMessage(ex));
        return build(HttpStatus.FORBIDDEN, "Access is denied", request);
    }

    // 401 - Authentication problems (Spring Security)
    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuth(org.springframework.security.core.AuthenticationException ex,
                                               HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Authentication is required", request);
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

    private String safeMessage(Throwable ex) {
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? ex.getClass().getSimpleName() : msg;
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
