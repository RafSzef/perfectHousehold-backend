package com.perfecthousehold.transport.request;

/**
 * Payload for changing current user's password.
 */
public record ChangePasswordRequest(String currentPassword, String newPassword) {
}
