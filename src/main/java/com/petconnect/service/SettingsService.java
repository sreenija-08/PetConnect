package com.petconnect.service;

import com.petconnect.entity.NotificationPreference;
import com.petconnect.entity.User;

public interface SettingsService {
    void updateProfile(User user, String fullName, String phone);
    void changePassword(User user, String currentPassword, String newPassword, String confirmPassword);
    NotificationPreference getOrCreatePreferences(User user);
    void updatePreferences(User user, NotificationPreference updatedPreferences);
}
