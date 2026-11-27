package com.perfecthousehold.transport.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Strongly typed request payload for login.
 */
public record LoginRequest(
        @NotBlank(message = "Username must not be blank") String username,
        @NotBlank(message = "Password must not be blank") String password
) {
}
