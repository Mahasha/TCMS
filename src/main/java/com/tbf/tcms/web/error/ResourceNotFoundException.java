package com.tbf.tcms.web.error;

/**
 * Thrown when a requested resource cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
