package com.perfecthousehold.generic.mapper;

/**
 * Thrown when mapping a JSON payload or database representation to a target entity fails.
 */
public class DbMappingException extends RuntimeException {

    public DbMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}
