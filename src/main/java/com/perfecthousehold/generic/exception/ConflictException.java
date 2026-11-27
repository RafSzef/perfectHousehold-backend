package com.perfecthousehold.generic.exception;

/**
 * Thrown when a requested operation conflicts with existing domain state (e.g. duplicate username or email).
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
