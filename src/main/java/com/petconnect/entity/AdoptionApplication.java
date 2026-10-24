package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "adoption_requests")
public class AdoptionApplication {

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdoptionStatus status = AdoptionStatus.PENDING;

    // Step 0: Readiness Checklist Responses
    @Column(name = "readiness_q1")
    private String readinessQ1; // Long-term responsibility (10–15 years)

    @Column(name = "readiness_q2")
    private String readinessQ2; // Daily time commitment

    @Column(name = "readiness_q3")
    private String readinessQ3; // Financial preparation

    @Column(name = "readiness_q4")
    private String readinessQ4; // Pet-friendly living arrangement & landlord/family approval

    // Step 1: About Applicant
    private String fullName;
    private String email;
    private String phone;
    private String city;
    private String occupation;

    // Step 2: Home & Pet Experience
    private String currentPets; // e.g. "Yes" or "No"
    private String previousPetExperience; // e.g. "Yes" or "No"
    private String homeType; // Apartment, Independent House, Other
    private String petPermission; // Yes, No, Not Applicable
    private String hoursAlone; // Less than 2 hours, 2–4 hours, 4–8 hours, More than 8 hours

    // Step 3: Adoption Questions
    @Column(columnDefinition = "TEXT")
    private String adoptionReason;

    @Column(columnDefinition = "TEXT")
    private String primaryCaretaker;

    private String preparedForExpenses; // Yes, No

    @Column(columnDefinition = "TEXT")
    private String additionalInformation;

    private boolean termsAccepted = true;

    // Review & Workflow details
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String shelterRemarks;

    private String visitSlot; // e.g. "7 September - 10:00 AM"

    private String shelterRecommendation; // RECOMMENDED, NOT_RECOMMENDED

    @Column(columnDefinition = "TEXT")
    private String adminRemarks;

    private String visitStatus; // REQUESTED, CONFIRMED, COMPLETED, CANCELLED, RESCHEDULED

    private LocalDateTime approvedAt;
    private LocalDateTime completedAt;
    private LocalDateTime handoverDate;

    public AdoptionApplication() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public AdoptionStatus getStatus() { return status; }
    public void setStatus(AdoptionStatus status) { this.status = status; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getOccupation() { return occupation; }
    public void setOccupation(String occupation) { this.occupation = occupation; }

    public String getCurrentPets() { return currentPets; }
    public void setCurrentPets(String currentPets) { this.currentPets = currentPets; }

    public String getPreviousPetExperience() { return previousPetExperience; }
    public void setPreviousPetExperience(String previousPetExperience) { this.previousPetExperience = previousPetExperience; }

    public String getHomeType() { return homeType; }
    public void setHomeType(String homeType) { this.homeType = homeType; }

    public String getPetPermission() { return petPermission; }
    public void setPetPermission(String petPermission) { this.petPermission = petPermission; }

    public String getHoursAlone() { return hoursAlone; }
    public void setHoursAlone(String hoursAlone) { this.hoursAlone = hoursAlone; }

    public String getAdoptionReason() { return adoptionReason; }
    public void setAdoptionReason(String adoptionReason) { this.adoptionReason = adoptionReason; }

    public String getPrimaryCaretaker() { return primaryCaretaker; }
    public void setPrimaryCaretaker(String primaryCaretaker) { this.primaryCaretaker = primaryCaretaker; }

    public String getPreparedForExpenses() { return preparedForExpenses; }
    public void setPreparedForExpenses(String preparedForExpenses) { this.preparedForExpenses = preparedForExpenses; }

    public String getAdditionalInformation() { return additionalInformation; }
    public void setAdditionalInformation(String additionalInformation) { this.additionalInformation = additionalInformation; }

    public boolean isTermsAccepted() { return termsAccepted; }
    public void setTermsAccepted(boolean termsAccepted) { this.termsAccepted = termsAccepted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getShelterRemarks() { return shelterRemarks; }
    public void setShelterRemarks(String shelterRemarks) { this.shelterRemarks = shelterRemarks; }

    public String getVisitSlot() { return visitSlot; }
    public void setVisitSlot(String visitSlot) { this.visitSlot = visitSlot; }

    public String getShelterRecommendation() { return shelterRecommendation; }
    public void setShelterRecommendation(String shelterRecommendation) { this.shelterRecommendation = shelterRecommendation; }

    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String adminRemarks) { this.adminRemarks = adminRemarks; }

    public LocalDateTime getHandoverDate() { return handoverDate; }
    public void setHandoverDate(LocalDateTime handoverDate) { this.handoverDate = handoverDate; }

    public String getVisitStatus() { return visitStatus; }
    public void setVisitStatus(String visitStatus) { this.visitStatus = visitStatus; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public String getStatusCode() {
        return status != null ? status.name() : "PENDING";
    }

    public String getStatusDisplayName() {
        return status != null ? status.getDisplayName() : "Pending Review";
    }

    // Aliases for compatibility
    public String getPriorExperience() { return previousPetExperience; }
    public void setPriorExperience(String priorExperience) { this.previousPetExperience = priorExperience; }

    public String getHoursAloneDaily() { return hoursAlone; }
    public void setHoursAloneDaily(String hoursAloneDaily) { this.hoursAlone = hoursAloneDaily; }

    public String getReasonForAdopting() { return adoptionReason; }
    public void setReasonForAdopting(String reasonForAdopting) { this.adoptionReason = reasonForAdopting; }

    public boolean isConsent() { return termsAccepted; }
    public void setConsent(boolean consent) { this.termsAccepted = consent; }

    public String getReadinessQ1() { return readinessQ1; }
    public void setReadinessQ1(String readinessQ1) { this.readinessQ1 = readinessQ1; }

    public String getReadinessQ2() { return readinessQ2; }
    public void setReadinessQ2(String readinessQ2) { this.readinessQ2 = readinessQ2; }

    public String getReadinessQ3() { return readinessQ3; }
    public void setReadinessQ3(String readinessQ3) { this.readinessQ3 = readinessQ3; }

    public String getReadinessQ4() { return readinessQ4; }
    public void setReadinessQ4(String readinessQ4) { this.readinessQ4 = readinessQ4; }
}
