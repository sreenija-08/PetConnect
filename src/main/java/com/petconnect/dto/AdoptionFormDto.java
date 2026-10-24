package com.petconnect.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdoptionFormDto {

    @NotNull
    private Long petId;

    // Step 0: Readiness Checklist Answers
    @NotBlank(message = "Please answer whether you are prepared for the long-term responsibility of caring for this pet")
    private String readinessQ1;

    @NotBlank(message = "Please answer whether you have enough daily time for the pet")
    private String readinessQ2;

    @NotBlank(message = "Please answer whether you are financially prepared for pet care expenses")
    private String readinessQ3;

    @NotBlank(message = "Please answer whether you have a pet-friendly living arrangement")
    private String readinessQ4;

    // Step 1: About You
    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String city;
    private String occupation;

    // Step 2: Your Home
    private String currentPets = "No";
    private String previousPetExperience = "Yes";
    private String homeType = "Apartment";
    private String petPermission = "Yes";
    private String hoursAlone = "2–4 hours";

    // Step 3: Adoption Motivation
    @NotBlank(message = "Please explain why you want to adopt this pet")
    private String adoptionReason;

    @NotBlank(message = "Please specify who will primarily take care of the pet")
    private String primaryCaretaker;

    private String preparedForExpenses = "Yes";

    private String additionalInformation;

    @AssertTrue(message = "You must accept the terms before submitting")
    private boolean termsAccepted;

    public AdoptionFormDto() {}

    // Getters and Setters
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

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

    public String getReadinessQ1() { return readinessQ1; }
    public void setReadinessQ1(String readinessQ1) { this.readinessQ1 = readinessQ1; }

    public String getReadinessQ2() { return readinessQ2; }
    public void setReadinessQ2(String readinessQ2) { this.readinessQ2 = readinessQ2; }

    public String getReadinessQ3() { return readinessQ3; }
    public void setReadinessQ3(String readinessQ3) { this.readinessQ3 = readinessQ3; }

    public String getReadinessQ4() { return readinessQ4; }
    public void setReadinessQ4(String readinessQ4) { this.readinessQ4 = readinessQ4; }
}
