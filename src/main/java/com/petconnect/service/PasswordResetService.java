package com.petconnect.service;

import com.petconnect.entity.PasswordResetToken;

public interface PasswordResetService {

    /**
     * Creates and sends a secure password reset token if the user exists.
     * Always returns without error to prevent account enumeration.
     */
    void processForgotPassword(String email, String appBaseUrl);

    /**
     * Validates a password reset token.
     * Returns the token if valid and unexpired, or null otherwise.
     */
    PasswordResetToken validateToken(String token);

    /**
     * Resets the user's password using a valid token and marks the token as used.
     */
    boolean resetPassword(String token, String newPassword);
}
