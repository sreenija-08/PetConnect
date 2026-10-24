package com.petconnect.service;

import com.petconnect.entity.PasswordResetToken;
import com.petconnect.entity.User;
import com.petconnect.repository.PasswordResetTokenRepository;
import com.petconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);
    private static final SecureRandom secureRandom = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordResetServiceImpl(UserRepository userRepository,
                                    PasswordResetTokenRepository tokenRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    @Transactional
    public void processForgotPassword(String email, String appBaseUrl) {
        if (email == null || email.isBlank()) {
            return;
        }
        String cleanEmail = email.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(cleanEmail);

        if (userOpt.isEmpty()) {
            log.info("Password reset requested for non-existent email: {} (suppressed enumeration)", cleanEmail);
            return;
        }

        User user = userOpt.get();

        // Invalidate any existing unused reset tokens for this user
        List<PasswordResetToken> activeTokens = tokenRepository.findByUserAndUsedFalse(user);
        for (PasswordResetToken activeToken : activeTokens) {
            activeToken.setUsed(true);
        }
        tokenRepository.saveAll(activeTokens);

        // Generate new secure token with 30 minutes validity
        String tokenString = generateSecureToken();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(30);

        PasswordResetToken resetToken = new PasswordResetToken(user, tokenString, expiryDate);
        tokenRepository.save(resetToken);

        // Build reset link
        String cleanBaseUrl = (appBaseUrl != null && !appBaseUrl.isBlank()) ? appBaseUrl : "http://localhost:8080";
        if (cleanBaseUrl.endsWith("/")) {
            cleanBaseUrl = cleanBaseUrl.substring(0, cleanBaseUrl.length() - 1);
        }
        String resetLink = cleanBaseUrl + "/reset-password?token=" + tokenString;

        // Send email via existing EmailService
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), resetLink);
        log.info("Password reset token generated and email dispatched for user: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public PasswordResetToken validateToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token.trim());
        if (tokenOpt.isEmpty()) {
            return null;
        }
        PasswordResetToken resetToken = tokenOpt.get();
        if (!resetToken.isValid()) {
            return null;
        }
        return resetToken;
    }

    @Override
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = validateToken(token);
        if (resetToken == null) {
            return false;
        }

        User user = resetToken.getUser();
        if (user == null) {
            return false;
        }

        // Update password using BCrypt
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("Password successfully reset for user: {}", user.getEmail());
        return true;
    }
}
