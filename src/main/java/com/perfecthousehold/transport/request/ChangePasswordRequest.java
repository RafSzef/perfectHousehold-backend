package com.perfecthousehold.transport.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for changing current user's password.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "Current password must not be blank") String currentPassword,
        @NotBlank(message = "New password must not be blank") String newPassword
) {
}
