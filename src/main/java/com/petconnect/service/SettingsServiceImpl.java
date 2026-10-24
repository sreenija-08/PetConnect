package com.petconnect.service;

import com.petconnect.entity.NotificationPreference;
import com.petconnect.entity.User;
import com.petconnect.repository.NotificationPreferenceRepository;
import com.petconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class SettingsServiceImpl implements SettingsService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{7,15}$");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<[^>]*>|javascript:|onerror|onload", Pattern.CASE_INSENSITIVE);

    private final UserRepository userRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public SettingsServiceImpl(UserRepository userRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void updateProfile(User user, String fullName, String phone) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name cannot be empty.");
        }

        String sanitizedName = fullName.trim();
        if (SCRIPT_PATTERN.matcher(sanitizedName).find()) {
            throw new IllegalArgumentException("Invalid characters detected in name.");
        }

        if (sanitizedName.length() < 2 || sanitizedName.length() > 100) {
            throw new IllegalArgumentException("Full Name must be between 2 and 100 characters.");
        }

        String sanitizedPhone = (phone != null) ? phone.trim().replaceAll("\\s+", "") : "";
        if (!sanitizedPhone.isEmpty()) {
            if (SCRIPT_PATTERN.matcher(sanitizedPhone).find() || !PHONE_PATTERN.matcher(sanitizedPhone).matches()) {
                throw new IllegalArgumentException("Please enter a valid phone number (e.g. 9876543210 or +919876543210).");
            }
        }

        user.setFullName(sanitizedName);
        user.setPhone(sanitizedPhone);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword, String confirmPassword) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (currentPassword == null || currentPassword.isEmpty()) {
            throw new IllegalArgumentException("Please enter your current password.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }

        if (confirmPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password do not match.");
        }

        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException("New password cannot be the same as the current password.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional
    public NotificationPreference getOrCreatePreferences(User user) {
        if (user == null) return null;
        return preferenceRepository.findByUser(user)
                .orElseGet(() -> {
                    NotificationPreference pref = new NotificationPreference(user);
                    return preferenceRepository.save(pref);
                });
    }

    @Override
    @Transactional
    public void updatePreferences(User user, NotificationPreference updatedPreferences) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        NotificationPreference existing = getOrCreatePreferences(user);
        if (updatedPreferences != null) {
            existing.setEmailAdoptionUpdates(updatedPreferences.isEmailAdoptionUpdates());
            existing.setEmailVisitUpdates(updatedPreferences.isEmailVisitUpdates());
            existing.setEmailCareUpdates(updatedPreferences.isEmailCareUpdates());
            existing.setEmailSecurityUpdates(updatedPreferences.isEmailSecurityUpdates());
            existing.setInappAdoptionUpdates(updatedPreferences.isInappAdoptionUpdates());
            existing.setInappCareUpdates(updatedPreferences.isInappCareUpdates());
            existing.setInappShelterUpdates(updatedPreferences.isInappShelterUpdates());
            existing.setInappSystemUpdates(updatedPreferences.isInappSystemUpdates());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        preferenceRepository.save(existing);
    }
}
