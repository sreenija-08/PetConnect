package com.petconnect.entity;

public enum AdoptionStatus {
    PENDING("Pending Review"),
    SHORTLISTED("Shortlisted"),
    VISIT_REQUESTED("Visit Requested"),
    VISIT_CONFIRMED("Visit Confirmed"),
    VISIT_COMPLETED("Visit Completed"),
    SHELTER_RECOMMENDED("Shelter Recommended"),
    ADMIN_REVIEW("Admin Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    COMPLETED("Adoption Completed");

    private final String displayName;

    AdoptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
