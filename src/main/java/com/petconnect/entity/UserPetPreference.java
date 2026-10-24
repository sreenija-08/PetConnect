package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_pet_preferences", uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_pet_preference", columnNames = {"user_id"})
})
public class UserPetPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String preferredPetType = "Any"; // Dog, Cat, Any

    private String activityLevel = "Medium"; // Low, Medium, High

    private String homeType = "House"; // Apartment, House, Farm / Large Property, Other

    private String experienceLevel = "First-time Owner"; // First-time Owner, Some Experience, Experienced

    private String timeAvailable = "2–4 hours"; // Less than 1 hour, 1–2 hours, 2–4 hours, More than 4 hours

    private Boolean hasChildren = false;

    private String preferredSize = "Any"; // Small, Medium, Large, Any

    private String preferredAge = "Any"; // Young, Adult, Senior, Any

    private String preferredLocation;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserPetPreference() {}

    public UserPetPreference(User user) {
        this.user = user;
        this.updatedAt = LocalDateTime.now();
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

    public String getPreferredPetType() {
        return preferredPetType != null ? preferredPetType : "Any";
    }

    public void setPreferredPetType(String preferredPetType) {
        this.preferredPetType = preferredPetType;
    }

    public String getActivityLevel() {
        return activityLevel != null ? activityLevel : "Medium";
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }

    public String getHomeType() {
        return homeType != null ? homeType : "House";
    }

    public void setHomeType(String homeType) {
        this.homeType = homeType;
    }

    public String getExperienceLevel() {
        return experienceLevel != null ? experienceLevel : "First-time Owner";
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public String getTimeAvailable() {
        return timeAvailable != null ? timeAvailable : "2–4 hours";
    }

    public void setTimeAvailable(String timeAvailable) {
        this.timeAvailable = timeAvailable;
    }

    public Boolean getHasChildren() {
        return Boolean.TRUE.equals(hasChildren);
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getPreferredSize() {
        return preferredSize != null ? preferredSize : "Any";
    }

    public void setPreferredSize(String preferredSize) {
        this.preferredSize = preferredSize;
    }

    public String getPreferredAge() {
        return preferredAge != null ? preferredAge : "Any";
    }

    public void setPreferredAge(String preferredAge) {
        this.preferredAge = preferredAge;
    }

    public String getPreferredLocation() {
        return preferredLocation;
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
