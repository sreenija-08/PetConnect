package com.petconnect.entity;

public enum CareBookingStatus {
    PENDING("Pending Approval"),
    ACCEPTED("Accepted / Confirmed"),
    REJECTED("Rejected"),
    CANCELLED("Cancelled"),
    CHECKED_IN("Checked In"),
    ACTIVE("Active Stay"),
    COMPLETED("Completed");

    private final String displayName;

    CareBookingStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
