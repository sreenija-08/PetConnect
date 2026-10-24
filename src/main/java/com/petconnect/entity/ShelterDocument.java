package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_documents")
public class ShelterDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @Column(nullable = false)
    private String documentUrl;

    private String originalFilename;

    private String fileType; // PDF, JPG, PNG, WEBP

    private Long fileSize;

    private String documentType; // SHELTER_REGISTRATION_CERTIFICATE, ANIMAL_WELFARE_LICENSE, GOVERNMENT_REGISTRATION, UNKNOWN

    @Column(name = "verification_mode")
    private String verificationMode = "PRODUCTION"; // PRODUCTION, DEMO

    @Column(name = "verification_status")
    private String verificationStatus = "PENDING"; // PENDING, PROCESSING, MANUAL_REVIEW, AUTO_REJECTED, APPROVED, REJECTED

    private Integer verificationConfidenceScore; // 0 - 100

    private String riskLevel; // LOW, MEDIUM, HIGH

    private Boolean autoRejected = false;

    private Boolean hardFail = false;

    @Column(name = "rejection_reason_code")
    private String rejectionReasonCode;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    private Boolean nameMatch;

    private Boolean addressMatch;

    private Boolean registrationNumberMatch;

    private Boolean registrationNumberDetected;

    private String extractedRegistrationNumber;

    private Boolean dateValid;

    private String issueDate;

    private String expiryDate;

    private Boolean qrDetected;

    private Boolean qrVerified = false;

    private String officialVerificationStatus = "NOT_AVAILABLE";

    private Boolean tamperingIndicators;

    private String ocrStatus; // SUCCESS, NO_TEXT, OCR_FAILED

    @Column(columnDefinition = "TEXT")
    private String verificationReasons;

    @Column(columnDefinition = "TEXT")
    private String aiAnalysisJson;

    private String adminDecision; // APPROVED, REJECTED

    @Column(columnDefinition = "TEXT")
    private String adminDecisionReason;

    private String verifiedBy;

    private LocalDateTime verifiedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();

    public ShelterDocument() {}

    public ShelterDocument(Shelter shelter, String documentUrl, String originalFilename, String fileType, Long fileSize) {
        this.shelter = shelter;
        this.documentUrl = documentUrl;
        this.originalFilename = originalFilename;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Integer getVerificationConfidenceScore() { return verificationConfidenceScore; }
    public void setVerificationConfidenceScore(Integer verificationConfidenceScore) { this.verificationConfidenceScore = verificationConfidenceScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public boolean isAutoRejected() { return Boolean.TRUE.equals(autoRejected); }
    public boolean getAutoRejected() { return Boolean.TRUE.equals(autoRejected); }
    public void setAutoRejected(Boolean autoRejected) { this.autoRejected = Boolean.TRUE.equals(autoRejected); }

    public boolean isHardFail() { return Boolean.TRUE.equals(hardFail); }
    public boolean getHardFail() { return Boolean.TRUE.equals(hardFail); }
    public void setHardFail(Boolean hardFail) { this.hardFail = Boolean.TRUE.equals(hardFail); }

    public String getRejectionReasonCode() { return rejectionReasonCode; }
    public void setRejectionReasonCode(String rejectionReasonCode) { this.rejectionReasonCode = rejectionReasonCode; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Boolean getNameMatch() { return nameMatch; }
    public void setNameMatch(Boolean nameMatch) { this.nameMatch = nameMatch; }

    public Boolean getAddressMatch() { return addressMatch; }
    public void setAddressMatch(Boolean addressMatch) { this.addressMatch = addressMatch; }

    public Boolean getRegistrationNumberMatch() { return registrationNumberMatch; }
    public void setRegistrationNumberMatch(Boolean registrationNumberMatch) { this.registrationNumberMatch = registrationNumberMatch; }

    public Boolean getRegistrationNumberDetected() { return registrationNumberDetected; }
    public void setRegistrationNumberDetected(Boolean registrationNumberDetected) { this.registrationNumberDetected = registrationNumberDetected; }

    public String getExtractedRegistrationNumber() { return extractedRegistrationNumber; }
    public void setExtractedRegistrationNumber(String extractedRegistrationNumber) { this.extractedRegistrationNumber = extractedRegistrationNumber; }

    public Boolean getDateValid() { return dateValid; }
    public void setDateValid(Boolean dateValid) { this.dateValid = dateValid; }

    public String getIssueDate() { return issueDate; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public Boolean getQrDetected() { return qrDetected; }
    public void setQrDetected(Boolean qrDetected) { this.qrDetected = qrDetected; }

    public Boolean getQrVerified() { return qrVerified; }
    public void setQrVerified(Boolean qrVerified) { this.qrVerified = qrVerified; }

    public String getOfficialVerificationStatus() { return officialVerificationStatus; }
    public void setOfficialVerificationStatus(String officialVerificationStatus) { this.officialVerificationStatus = officialVerificationStatus; }

    public Boolean getTamperingIndicators() { return tamperingIndicators; }
    public void setTamperingIndicators(Boolean tamperingIndicators) { this.tamperingIndicators = tamperingIndicators; }

    public String getOcrStatus() { return ocrStatus; }
    public void setOcrStatus(String ocrStatus) { this.ocrStatus = ocrStatus; }

    public String getVerificationReasons() { return verificationReasons; }
    public void setVerificationReasons(String verificationReasons) { this.verificationReasons = verificationReasons; }

    public String getAiAnalysisJson() { return aiAnalysisJson; }
    public void setAiAnalysisJson(String aiAnalysisJson) { this.aiAnalysisJson = aiAnalysisJson; }

    public String getAdminDecision() { return adminDecision; }
    public void setAdminDecision(String adminDecision) { this.adminDecision = adminDecision; }

    public String getAdminDecisionReason() { return adminDecisionReason; }
    public void setAdminDecisionReason(String adminDecisionReason) { this.adminDecisionReason = adminDecisionReason; }

    public String getVerifiedBy() { return verifiedBy; }
    public void setVerifiedBy(String verifiedBy) { this.verifiedBy = verifiedBy; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerificationMode() { return verificationMode != null ? verificationMode : "PRODUCTION"; }
    public void setVerificationMode(String verificationMode) { this.verificationMode = verificationMode; }
    public boolean isDemoMode() { return "DEMO".equalsIgnoreCase(this.verificationMode); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
