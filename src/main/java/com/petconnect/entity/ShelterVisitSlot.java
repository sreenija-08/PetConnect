package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_visit_slots")
public class ShelterVisitSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "application_id")
    private AdoptionApplication application;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private String timeSlot; // e.g. "10:00 AM", "11:00 AM", "02:00 PM", "04:00 PM"

    @Column(nullable = false)
    private String status = "AVAILABLE"; // AVAILABLE, REQUESTED, CONFIRMED, COMPLETED, CANCELLED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "booked_by_user_id")
    private User bookedBy;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    public ShelterVisitSlot() {}

    public ShelterVisitSlot(Shelter shelter, Pet pet, AdoptionApplication application, LocalDate visitDate, String timeSlot) {
        this.shelter = shelter;
        this.pet = pet;
        this.application = application;
        this.visitDate = visitDate;
        this.timeSlot = timeSlot;
        this.status = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public AdoptionApplication getApplication() { return application; }
    public void setApplication(AdoptionApplication application) { this.application = application; }

    public AdoptionApplication getAdoptionRequest() { return application; }
    public void setAdoptionRequest(AdoptionApplication req) { this.application = req; }

    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public User getBookedBy() { return bookedBy; }
    public void setBookedBy(User bookedBy) { this.bookedBy = bookedBy; }

    public User getUser() { return bookedBy; }
    public void setUser(User user) { this.bookedBy = user; }

    public String getVisitTime() { return timeSlot; }
    public void setVisitTime(String visitTime) { this.timeSlot = visitTime; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String getDisplayLabel() {
        return visitDate + " at " + timeSlot;
    }
}
