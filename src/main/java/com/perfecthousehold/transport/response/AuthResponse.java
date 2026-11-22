package com.perfecthousehold.transport.response;

/**
 * Strongly typed response for successful authentication returning JWT token.
 */
public record AuthResponse(String accessToken, String tokenType) {

    public static AuthResponse of(String token) {
        return new AuthResponse(token, "Bearer");
    }
}
