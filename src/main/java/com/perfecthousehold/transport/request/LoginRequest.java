package com.perfecthousehold.transport.request;

/**
 * Strongly typed request payload for login.
 */
public record LoginRequest(String username, String password) {
}
