package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "adoption_meetings")
public class AdoptionMeeting {

    public static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");
    public static final int EARLY_JOIN_MINUTES = 5;  // Allowed 5 minutes before scheduled start
    public static final int LATE_JOIN_MINUTES = 15;  // Allowed up to 15 minutes after duration ends

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id", nullable = false)
    private AdoptionApplication adoptionApplication;

    @Column(nullable = false)
    private LocalDate meetingDate;

    @Column(nullable = false)
    private LocalTime meetingTime;

    @Column(nullable = false)
    private Integer durationMinutes = 30; // default 30 mins

    @Column(nullable = false, unique = true)
    private String roomName; // e.g. "PetConnect-Adoption-14-a9f8b2c4"

    @Column(nullable = false)
    private String status = "SCHEDULED"; // SCHEDULED, LIVE, COMPLETED, CANCELLED, RESCHEDULED

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String cancellationReason;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public AdoptionMeeting() {}

    public AdoptionMeeting(AdoptionApplication adoptionApplication, LocalDate meetingDate, LocalTime meetingTime, Integer durationMinutes, String roomName, String notes, User createdBy) {
        this.adoptionApplication = adoptionApplication;
        this.meetingDate = meetingDate;
        this.meetingTime = meetingTime;
        this.durationMinutes = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 30;
        this.roomName = roomName;
        this.notes = notes;
        this.createdBy = createdBy;
        this.status = "SCHEDULED";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ==========================================
    // SCHEDULE & TIMEZONE CALCULATION (Asia/Kolkata)
    // ==========================================

    public ZonedDateTime getStartZonedDateTime() {
        if (meetingDate == null || meetingTime == null) return null;
        return ZonedDateTime.of(meetingDate, meetingTime, ZONE_ID);
    }

    public ZonedDateTime getEndZonedDateTime() {
        ZonedDateTime start = getStartZonedDateTime();
        if (start == null) return null;
        int duration = (durationMinutes != null && durationMinutes > 0) ? durationMinutes : 30;
        return start.plusMinutes(duration);
    }

    public ZonedDateTime getEarliestJoinZonedDateTime() {
        ZonedDateTime start = getStartZonedDateTime();
        if (start == null) return null;
        return start.minusMinutes(EARLY_JOIN_MINUTES);
    }

    public ZonedDateTime getLatestJoinZonedDateTime() {
        ZonedDateTime end = getEndZonedDateTime();
        if (end == null) return null;
        return end.plusMinutes(LATE_JOIN_MINUTES);
    }

    /**
     * Checks if the current time is strictly before the allowed joining window.
     */
    public boolean isBeforeMeetingWindow() {
        if (isCancelled() || isCompleted()) return false;
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime early = getEarliestJoinZonedDateTime();
        return early != null && now.isBefore(early);
    }

    /**
     * Checks if current time is within the valid active meeting window [start - 5m, end + 15m].
     */
    public boolean isDuringMeetingWindow() {
        if (isCancelled() || isCompleted()) return false;
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime early = getEarliestJoinZonedDateTime();
        ZonedDateTime late = getLatestJoinZonedDateTime();
        return early != null && late != null && !now.isBefore(early) && !now.isAfter(late);
    }

    /**
     * Checks if the meeting window has fully passed.
     */
    public boolean isAfterMeetingWindow() {
        if (isCancelled()) return false;
        if (isCompleted()) return true;
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime late = getLatestJoinZonedDateTime();
        return late != null && now.isAfter(late);
    }

    /**
     * Computes the dynamic lifecycle state:
     * - CANCELLED
     * - SCHEDULED (Upcoming, before start window)
     * - LIVE (Currently active and accessible)
     * - COMPLETED (Window passed or marked completed)
     */
    public String getMeetingLifecycleState() {
        if (isCancelled()) return "CANCELLED";
        if (isCompleted()) return "COMPLETED";
        ZonedDateTime now = ZonedDateTime.now(ZONE_ID);
        ZonedDateTime early = getEarliestJoinZonedDateTime();
        ZonedDateTime late = getLatestJoinZonedDateTime();
        if (early != null && now.isBefore(early)) {
            return "SCHEDULED";
        }
        if (late != null && now.isAfter(late)) {
            return "COMPLETED";
        }
        return "LIVE";
    }

    // Helper display formatting methods
    public String getFormattedDate() {
        if (meetingDate == null) return "";
        return meetingDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
    }

    public String getFormattedTime() {
        if (meetingTime == null) return "";
        return meetingTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedEndTime() {
        ZonedDateTime end = getEndZonedDateTime();
        if (end == null) return "";
        return end.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public String getFormattedDateTime() {
        return getFormattedDate() + " at " + getFormattedTime();
    }

    public String getFormattedTimeRange() {
        return getFormattedTime() + " – " + getFormattedEndTime();
    }

    public boolean isScheduled() {
        return "SCHEDULED".equalsIgnoreCase(this.status) || "STARTED".equalsIgnoreCase(this.status) || "LIVE".equalsIgnoreCase(this.status);
    }

    public boolean isCompleted() {
        return "COMPLETED".equalsIgnoreCase(this.status);
    }

    public boolean isCancelled() {
        return "CANCELLED".equalsIgnoreCase(this.status);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AdoptionApplication getAdoptionApplication() { return adoptionApplication; }
    public void setAdoptionApplication(AdoptionApplication adoptionApplication) { this.adoptionApplication = adoptionApplication; }

    public LocalDate getMeetingDate() { return meetingDate; }
    public void setMeetingDate(LocalDate meetingDate) { this.meetingDate = meetingDate; }

    public LocalTime getMeetingTime() { return meetingTime; }
    public void setMeetingTime(LocalTime meetingTime) { this.meetingTime = meetingTime; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
