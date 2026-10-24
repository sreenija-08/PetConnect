package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_verification_audits")
public class ShelterVerificationAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long shelterId;

    private Long documentId;

    private Integer verificationScore;

    private String riskLevel;

    private Boolean hardFail = false;

    private String decision; // AUTO_REJECTED, MANUAL_REVIEW, LOW_RISK_ADMIN_REVIEW, ADMIN_APPROVED, ADMIN_REJECTED

    private String rejectionReasonCode;

    @Column(columnDefinition = "TEXT")
    private String reasons;

    @Column(name = "verification_mode")
    private String verificationMode = "PRODUCTION";

    private String performedBy = "SYSTEM_AI";

    private String aiVersion = "PetConnect-DocAI-v1.0";

    private LocalDateTime timestamp = LocalDateTime.now();

    public ShelterVerificationAudit() {}

    public ShelterVerificationAudit(Long shelterId, Long documentId, Integer verificationScore,
                                    String riskLevel, Boolean hardFail, String decision,
                                    String rejectionReasonCode, String reasons, String performedBy) {
        this(shelterId, documentId, verificationScore, riskLevel, hardFail, decision, rejectionReasonCode, reasons, performedBy, "PRODUCTION");
    }

    public ShelterVerificationAudit(Long shelterId, Long documentId, Integer verificationScore,
                                    String riskLevel, Boolean hardFail, String decision,
                                    String rejectionReasonCode, String reasons, String performedBy,
                                    String verificationMode) {
        this.shelterId = shelterId;
        this.documentId = documentId;
        this.verificationScore = verificationScore;
        this.riskLevel = riskLevel;
        this.hardFail = hardFail;
        this.decision = decision;
        this.rejectionReasonCode = rejectionReasonCode;
        this.reasons = reasons;
        this.performedBy = performedBy;
        this.verificationMode = (verificationMode != null && !verificationMode.isBlank()) ? verificationMode : "PRODUCTION";
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getShelterId() { return shelterId; }
    public void setShelterId(Long shelterId) { this.shelterId = shelterId; }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Integer getVerificationScore() { return verificationScore; }
    public void setVerificationScore(Integer verificationScore) { this.verificationScore = verificationScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Boolean isHardFail() { return Boolean.TRUE.equals(hardFail); }
    public Boolean getHardFail() { return hardFail; }
    public void setHardFail(Boolean hardFail) { this.hardFail = hardFail; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }

    public String getRejectionReasonCode() { return rejectionReasonCode; }
    public void setRejectionReasonCode(String rejectionReasonCode) { this.rejectionReasonCode = rejectionReasonCode; }

    public String getReasons() { return reasons; }
    public void setReasons(String reasons) { this.reasons = reasons; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getAiVersion() { return aiVersion; }
    public void setAiVersion(String aiVersion) { this.aiVersion = aiVersion; }

    public String getVerificationMode() { return verificationMode != null ? verificationMode : "PRODUCTION"; }
    public void setVerificationMode(String verificationMode) { this.verificationMode = verificationMode; }
    public boolean isDemoMode() { return "DEMO".equalsIgnoreCase(this.verificationMode); }

    // Template aliases
    public String getActionTaken() { return decision; }
    public String getActedBy() { return performedBy; }
    public Integer getConfidenceScore() { return verificationScore; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
