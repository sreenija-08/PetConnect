package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    // Email Notifications
    @Column(name = "email_adoption_updates")
    private boolean emailAdoptionUpdates = true;

    @Column(name = "email_visit_updates")
    private boolean emailVisitUpdates = true;

    @Column(name = "email_care_updates")
    private boolean emailCareUpdates = true;

    @Column(name = "email_security_updates")
    private boolean emailSecurityUpdates = true;

    // In-App Notifications
    @Column(name = "inapp_adoption_updates")
    private boolean inappAdoptionUpdates = true;

    @Column(name = "inapp_care_updates")
    private boolean inappCareUpdates = true;

    @Column(name = "inapp_shelter_updates")
    private boolean inappShelterUpdates = true;

    @Column(name = "inapp_system_updates")
    private boolean inappSystemUpdates = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public NotificationPreference() {}

    public NotificationPreference(User user) {
        this.user = user;
        this.emailAdoptionUpdates = true;
        this.emailVisitUpdates = true;
        this.emailCareUpdates = true;
        this.emailSecurityUpdates = true;
        this.inappAdoptionUpdates = true;
        this.inappCareUpdates = true;
        this.inappShelterUpdates = true;
        this.inappSystemUpdates = true;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isEmailAdoptionUpdates() {
        return emailAdoptionUpdates;
    }

    public void setEmailAdoptionUpdates(boolean emailAdoptionUpdates) {
        this.emailAdoptionUpdates = emailAdoptionUpdates;
    }

    public boolean isEmailVisitUpdates() {
        return emailVisitUpdates;
    }

    public void setEmailVisitUpdates(boolean emailVisitUpdates) {
        this.emailVisitUpdates = emailVisitUpdates;
    }

    public boolean isEmailCareUpdates() {
        return emailCareUpdates;
    }

    public void setEmailCareUpdates(boolean emailCareUpdates) {
        this.emailCareUpdates = emailCareUpdates;
    }

    public boolean isEmailSecurityUpdates() {
        return emailSecurityUpdates;
    }

    public void setEmailSecurityUpdates(boolean emailSecurityUpdates) {
        this.emailSecurityUpdates = emailSecurityUpdates;
    }

    public boolean isInappAdoptionUpdates() {
        return inappAdoptionUpdates;
    }

    public void setInappAdoptionUpdates(boolean inappAdoptionUpdates) {
        this.inappAdoptionUpdates = inappAdoptionUpdates;
    }

    public boolean isInappCareUpdates() {
        return inappCareUpdates;
    }

    public void setInappCareUpdates(boolean inappCareUpdates) {
        this.inappCareUpdates = inappCareUpdates;
    }

    public boolean isInappShelterUpdates() {
        return inappShelterUpdates;
    }

    public void setInappShelterUpdates(boolean inappShelterUpdates) {
        this.inappShelterUpdates = inappShelterUpdates;
    }

    public boolean isInappSystemUpdates() {
        return inappSystemUpdates;
    }

    public void setInappSystemUpdates(boolean inappSystemUpdates) {
        this.inappSystemUpdates = inappSystemUpdates;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
