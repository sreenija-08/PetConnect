package com.petconnect.service;

import com.petconnect.entity.Notification;
import com.petconnect.entity.User;
import com.petconnect.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final com.petconnect.repository.NotificationPreferenceRepository preferenceRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   com.petconnect.repository.NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public Notification createNotification(User recipient, String title, String message, String type) {
        if (recipient == null) return null;

        // Respect in-app notification preferences
        if (preferenceRepository != null) {
            java.util.Optional<com.petconnect.entity.NotificationPreference> optPref = preferenceRepository.findByUser(recipient);
            if (optPref.isPresent()) {
                com.petconnect.entity.NotificationPreference pref = optPref.get();
                String upperType = (type != null) ? type.toUpperCase() : "";
                if (upperType.contains("ADOPT") && !pref.isInappAdoptionUpdates()) {
                    return null;
                }
                if ((upperType.contains("CARE") || upperType.contains("BOOK")) && !pref.isInappCareUpdates()) {
                    return null;
                }
                if (upperType.contains("SHELTER") && !pref.isInappShelterUpdates()) {
                    return null;
                }
                if (upperType.contains("SYSTEM") && !pref.isInappSystemUpdates()) {
                    return null;
                }
            }
        }

        Notification notification = new Notification(recipient, title, message, type);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        if (user == null) return List.of();
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
    }

    @Override
    public long getUnreadCount(User user) {
        if (user == null) return 0;
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> list = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        for (Notification n : list) {
            n.setRead(true);
        }
        notificationRepository.saveAll(list);
    }
}
