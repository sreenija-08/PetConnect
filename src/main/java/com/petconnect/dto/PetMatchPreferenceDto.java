package com.petconnect.dto;

import com.petconnect.entity.UserPetPreference;

public class PetMatchPreferenceDto {

    private String preferredPetType = "Any"; // Dog, Cat, Any
    private String activityLevel = "Medium"; // Low, Medium, High
    private String homeType = "House"; // Apartment, House, Farm / Large Property, Other
    private String experienceLevel = "First-time Owner"; // First-time Owner, Some Experience, Experienced
    private String timeAvailable = "2–4 hours"; // Less than 1 hour, 1–2 hours, 2–4 hours, More than 4 hours
    private Boolean hasChildren = false; // Yes / No
    private String preferredSize = "Any"; // Small, Medium, Large, Any
    private String preferredAge = "Any"; // Young, Adult, Senior, Any
    private String preferredLocation = "";
    private Boolean savePreferences = true;

    public PetMatchPreferenceDto() {}

    public static PetMatchPreferenceDto fromEntity(UserPetPreference entity) {
        PetMatchPreferenceDto dto = new PetMatchPreferenceDto();
        if (entity != null) {
            dto.setPreferredPetType(entity.getPreferredPetType());
            dto.setActivityLevel(entity.getActivityLevel());
            dto.setHomeType(entity.getHomeType());
            dto.setExperienceLevel(entity.getExperienceLevel());
            dto.setTimeAvailable(entity.getTimeAvailable());
            dto.setHasChildren(entity.getHasChildren());
            dto.setPreferredSize(entity.getPreferredSize());
            dto.setPreferredAge(entity.getPreferredAge());
            dto.setPreferredLocation(entity.getPreferredLocation());
            dto.setSavePreferences(true);
        }
        return dto;
    }

    // Getters and Setters
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
        return preferredLocation != null ? preferredLocation : "";
    }

    public void setPreferredLocation(String preferredLocation) {
        this.preferredLocation = preferredLocation;
    }

    public Boolean getSavePreferences() {
        return savePreferences != null ? savePreferences : true;
    }

    public void setSavePreferences(Boolean savePreferences) {
        this.savePreferences = savePreferences;
    }
}
