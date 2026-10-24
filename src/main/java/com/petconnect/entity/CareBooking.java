package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "care_bookings")
public class CareBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Integer numberOfDays = 1;

    private Double boardingFeePerDay = 300.0;

    private Double totalFee = 300.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareBookingStatus status = CareBookingStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String specialInstructions;

    private String emergencyContact;

    private String vaccinationInfo;

    @Column(columnDefinition = "TEXT")
    private String medicalInformation;

    @Column(columnDefinition = "TEXT")
    private String additionalNotes;

    @Column(columnDefinition = "TEXT")
    private String shelterRemarks;

    private LocalDateTime requestedAt = LocalDateTime.now();

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    private LocalDateTime checkInDateTime;

    private LocalDateTime checkOutDateTime;

    public CareBooking() {}

    public CareBooking(User user, Pet pet, Shelter shelter, LocalDate startDate, LocalDate endDate,
                       Double boardingFeePerDay, String specialInstructions, String emergencyContact) {
        this.user = user;
        this.pet = pet;
        this.shelter = shelter;
        this.startDate = startDate;
        this.endDate = endDate;
        this.boardingFeePerDay = boardingFeePerDay != null ? boardingFeePerDay : 300.0;
        this.specialInstructions = specialInstructions;
        this.emergencyContact = emergencyContact;
        this.requestedAt = LocalDateTime.now();
        this.status = CareBookingStatus.PENDING;
        recalculateFee();
    }

    public void recalculateFee() {
        if (startDate != null && endDate != null) {
            long days = ChronoUnit.DAYS.between(startDate, endDate);
            this.numberOfDays = (int) Math.max(1, days);
            this.totalFee = this.numberOfDays * (boardingFeePerDay != null ? boardingFeePerDay : 300.0);
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
        recalculateFee();
    }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
        recalculateFee();
    }

    public Integer getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(Integer numberOfDays) { this.numberOfDays = numberOfDays; }

    public Double getBoardingFeePerDay() { return boardingFeePerDay; }
    public void setBoardingFeePerDay(Double boardingFeePerDay) { 
        this.boardingFeePerDay = boardingFeePerDay; 
        recalculateFee();
    }

    public Double getTotalFee() { return totalFee; }
    public void setTotalFee(Double totalFee) { this.totalFee = totalFee; }

    public CareBookingStatus getStatus() { return status; }
    public void setStatus(CareBookingStatus status) { this.status = status; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getVaccinationInfo() { return vaccinationInfo; }
    public void setVaccinationInfo(String vaccinationInfo) { this.vaccinationInfo = vaccinationInfo; }

    public String getMedicalInformation() { return medicalInformation; }
    public void setMedicalInformation(String medicalInformation) { this.medicalInformation = medicalInformation; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }

    public String getShelterRemarks() { return shelterRemarks; }
    public void setShelterRemarks(String shelterRemarks) { this.shelterRemarks = shelterRemarks; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(LocalDateTime rejectedAt) { this.rejectedAt = rejectedAt; }

    public LocalDateTime getCheckInDateTime() { return checkInDateTime; }
    public void setCheckInDateTime(LocalDateTime checkInDateTime) { this.checkInDateTime = checkInDateTime; }

    public LocalDateTime getCheckOutDateTime() { return checkOutDateTime; }
    public void setCheckOutDateTime(LocalDateTime checkOutDateTime) { this.checkOutDateTime = checkOutDateTime; }

    public String getStatusCode() {
        return status != null ? status.name() : "PENDING";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Pending Approval";
    }

    public String getPetName() {
        return pet != null ? pet.getName() : "Pet";
    }

    public String getPetType() {
        return pet != null ? pet.getType() : "DOG";
    }

    public String getPetBreed() {
        return pet != null ? pet.getBreed() : "";
    }

    public String getOwnerName() {
        return user != null ? (user.getFullName() != null ? user.getFullName() : user.getEmail()) : "Owner";
    }

    public String getShelterName() {
        return shelter != null ? shelter.getName() : "Shelter";
    }

    public String getRejectionReason() {
        return shelterRemarks;
    }

    public void setRejectionReason(String rejectionReason) {
        this.shelterRemarks = rejectionReason;
    }

    public String getOwnerNotes() {
        return additionalNotes;
    }

    public void setOwnerNotes(String ownerNotes) {
        this.additionalNotes = ownerNotes;
    }

    public LocalDateTime getCreatedAt() {
        return requestedAt;
    }

    public LocalDateTime getAcceptedDate() {
        return approvedAt;
    }

    public LocalDateTime getCheckInDate() {
        return checkInDateTime;
    }

    public LocalDateTime getCheckOutDate() {
        return checkOutDateTime;
    }

    public LocalDateTime getRequestDate() {
        return requestedAt;
    }

    public LocalDateTime getBookingDate() {
        return requestedAt;
    }
}
