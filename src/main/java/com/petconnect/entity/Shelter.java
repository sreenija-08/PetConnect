package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "shelters")
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String location;

    private String address;

    private String area;

    private String city;

    private String state;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer capacity = 20;

    private Integer availableCapacity = 5;

    @Column(name = "verification_status")
    private String verificationStatus = "PENDING"; // PENDING, APPROVED, REJECTED, INACTIVE

    @Column(name = "status")
    private String status = "ACTIVE"; // ACTIVE, INACTIVE

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private LocalDateTime verifiedAt;

    private String verifiedBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Verification Documents
    private String registrationNumber;

    @Column(name = "verification_mode")
    private String verificationMode = "PRODUCTION"; // PRODUCTION, DEMO

    private Integer verificationConfidenceScore; // 0 - 100

    private String riskLevel; // LOW, MEDIUM, HIGH

    private Boolean autoRejected = false;

    private Boolean hardFail = false;

    @Column(name = "rejection_reason_code")
    private String rejectionReasonCode;

    @Column(columnDefinition = "TEXT")
    private String verificationReasons;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysisJson;

    private String registrationDocument;

    private String identityDocument;

    private String addressProof;

    private String otherDocument;

    private boolean temporaryCareEnabled = true;

    private String phone;

    private String email;

    private Double boardingFee = 300.0;

    private String supportedPetTypes = "DOG,CAT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    public Shelter() {}

    public Shelter(String name, String location, String description, Integer capacity, 
                   Integer availableCapacity, String verificationStatus, boolean temporaryCareEnabled, 
                   String phone, String email, User owner) {
        this.name = name;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.availableCapacity = availableCapacity;
        this.verificationStatus = verificationStatus;
        this.temporaryCareEnabled = temporaryCareEnabled;
        this.phone = phone;
        this.email = email;
        this.owner = owner;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) { this.availableCapacity = availableCapacity; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getStatus() { return status != null ? status : "ACTIVE"; }
    public void setStatus(String status) { this.status = status; }
    public boolean isActive() { return !"INACTIVE".equalsIgnoreCase(this.status) && !"INACTIVE".equalsIgnoreCase(this.verificationStatus); }

    public boolean isTemporaryCareEnabled() { return temporaryCareEnabled; }
    public void setTemporaryCareEnabled(boolean temporaryCareEnabled) { this.temporaryCareEnabled = temporaryCareEnabled; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Double getBoardingFee() { return boardingFee != null ? boardingFee : 300.0; }
    public void setBoardingFee(Double boardingFee) { this.boardingFee = boardingFee; }

    public String getSupportedPetTypes() { return supportedPetTypes != null ? supportedPetTypes : "DOG,CAT"; }
    public void setSupportedPetTypes(String supportedPetTypes) { this.supportedPetTypes = supportedPetTypes; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getRegistrationDocument() { return registrationDocument; }
    public void setRegistrationDocument(String registrationDocument) { this.registrationDocument = registrationDocument; }

    public String getIdentityDocument() { return identityDocument; }
    public void setIdentityDocument(String identityDocument) { this.identityDocument = identityDocument; }

    public String getAddressProof() { return addressProof; }
    public void setAddressProof(String addressProof) { this.addressProof = addressProof; }

    public String getOtherDocument() { return otherDocument; }
    public void setOtherDocument(String otherDocument) { this.otherDocument = otherDocument; }

    public boolean supportsPetType(String petType) {
        if (petType == null || petType.isBlank() || "all".equalsIgnoreCase(petType)) return true;
        if (this.supportedPetTypes == null) return true;
        return this.supportedPetTypes.toUpperCase().contains(petType.trim().toUpperCase());
    }

    @OneToMany(mappedBy = "shelter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ShelterImage> images = new ArrayList<>();

    public List<ShelterImage> getImages() {
        return images;
    }

    public void setImages(List<ShelterImage> images) {
        this.images = images;
    }

    public String getFirstImage() {
        if (images != null && !images.isEmpty()) {
            return images.get(0).getImageUrl();
        }
        return null;
    }

    public String getDisplayImage() {
        if (images != null && !images.isEmpty() && images.get(0).getImageUrl() != null && !images.get(0).getImageUrl().isBlank()) {
            return images.get(0).getImageUrl();
        }
        return "https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&q=80&w=600";
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public Integer getVerificationConfidenceScore() { return verificationConfidenceScore; }
    public void setVerificationConfidenceScore(Integer verificationConfidenceScore) { this.verificationConfidenceScore = verificationConfidenceScore; }

    public String getRiskLevel() { return riskLevel != null ? riskLevel : "UNKNOWN"; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public boolean isAutoRejected() { return Boolean.TRUE.equals(autoRejected); }
    public boolean getAutoRejected() { return Boolean.TRUE.equals(autoRejected); }
    public void setAutoRejected(Boolean autoRejected) { this.autoRejected = Boolean.TRUE.equals(autoRejected); }

    public boolean isHardFail() { return Boolean.TRUE.equals(hardFail); }
    public boolean getHardFail() { return Boolean.TRUE.equals(hardFail); }
    public void setHardFail(Boolean hardFail) { this.hardFail = Boolean.TRUE.equals(hardFail); }

    public String getRejectionReasonCode() { return rejectionReasonCode; }
    public void setRejectionReasonCode(String rejectionReasonCode) { this.rejectionReasonCode = rejectionReasonCode; }

    public String getVerificationReasons() { return verificationReasons; }
    public void setVerificationReasons(String verificationReasons) { this.verificationReasons = verificationReasons; }

    public String getAiAnalysisJson() { return aiAnalysisJson; }
    public void setAiAnalysisJson(String aiAnalysisJson) { this.aiAnalysisJson = aiAnalysisJson; }

    public String getVerificationMode() { return verificationMode != null ? verificationMode : "PRODUCTION"; }
    public void setVerificationMode(String verificationMode) { this.verificationMode = verificationMode; }
    public boolean isDemoMode() { return "DEMO".equalsIgnoreCase(this.verificationMode); }

    public String getAiRecommendation() {
        if (isDemoMode()) {
            if ("DEMO_VALIDATION_PASSED".equalsIgnoreCase(verificationStatus)) {
                return "DEMO VALIDATION PASSED";
            }
            if ("DEMO_VALIDATION_FAILED".equalsIgnoreCase(verificationStatus) || Boolean.TRUE.equals(autoRejected)) {
                return "DEMO VALIDATION FAILED";
            }
            return "DEMO MANUAL REVIEW";
        }
        if (Boolean.TRUE.equals(autoRejected) || "AUTO_REJECTED".equalsIgnoreCase(verificationStatus)) {
            return "AUTO REJECTED";
        }
        if ("MANUAL_REVIEW".equalsIgnoreCase(verificationStatus)) {
            return "MANUAL REVIEW REQUIRED";
        }
        if ("APPROVED".equalsIgnoreCase(verificationStatus)) {
            return "APPROVED";
        }
        if ("REJECTED".equalsIgnoreCase(verificationStatus)) {
            return "REJECTED";
        }
        if (verificationConfidenceScore != null && verificationConfidenceScore >= 85) {
            return "LOW RISK - ADMIN REVIEW";
        }
        if (verificationConfidenceScore != null && verificationConfidenceScore >= 40) {
            return "MANUAL REVIEW REQUIRED";
        }
        return "PENDING REVIEW";
    }

    public List<String> getVerificationReasonsList() {
        if (verificationReasons == null || verificationReasons.isBlank()) {
            return java.util.Collections.emptyList();
        }
        String trimmed = verificationReasons.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(trimmed, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            } catch (Exception ignored) {}
        }
        return java.util.Arrays.stream(trimmed.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(java.util.stream.Collectors.toList());
    }
}
