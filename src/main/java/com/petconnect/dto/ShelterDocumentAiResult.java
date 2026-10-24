package com.petconnect.dto;

import java.util.ArrayList;
import java.util.List;

public class ShelterDocumentAiResult {

    private String documentType;
    private Integer confidenceScore; // 0 - 100
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String verificationStatus; // PENDING, MANUAL_REVIEW, AUTO_REJECTED, APPROVED, REJECTED
    private boolean autoRejected;
    private boolean hardFail;
    private String rejectionReasonCode;
    private String rejectionReason;

    private Boolean nameMatch;
    private Boolean addressMatch;
    private Boolean registrationNumberDetected;
    private Boolean registrationNumberMatch;
    private String extractedRegistrationNumber;

    private Boolean dateValid;
    private String issueDate;
    private String expiryDate;

    private Boolean qrDetected;
    private Boolean qrVerified;
    private String officialVerification = "NOT_AVAILABLE";

    private Boolean tamperingIndicators;
    private String ocrStatus;
    private List<String> reasons = new ArrayList<>();
    private String rawJson;

    private String verificationMode = "PRODUCTION";
    private boolean isDemo = false;
    private String demoNotice;
    private java.util.Map<String, Object> demoVerificationReport;

    public ShelterDocumentAiResult() {}

    // Getters and Setters
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }

    public Integer getConfidenceScore() { return confidenceScore != null ? confidenceScore : 0; }
    public void setConfidenceScore(Integer confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getRiskLevel() { return riskLevel != null ? riskLevel : "UNKNOWN"; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public boolean isAutoRejected() { return autoRejected; }
    public void setAutoRejected(boolean autoRejected) { this.autoRejected = autoRejected; }

    public boolean isHardFail() { return hardFail; }
    public void setHardFail(boolean hardFail) { this.hardFail = hardFail; }

    public String getRejectionReasonCode() { return rejectionReasonCode; }
    public void setRejectionReasonCode(String rejectionReasonCode) { this.rejectionReasonCode = rejectionReasonCode; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public Boolean getNameMatch() { return nameMatch; }
    public void setNameMatch(Boolean nameMatch) { this.nameMatch = nameMatch; }

    public Boolean getAddressMatch() { return addressMatch; }
    public void setAddressMatch(Boolean addressMatch) { this.addressMatch = addressMatch; }

    public Boolean getRegistrationNumberDetected() { return registrationNumberDetected; }
    public void setRegistrationNumberDetected(Boolean registrationNumberDetected) { this.registrationNumberDetected = registrationNumberDetected; }

    public Boolean getRegistrationNumberMatch() { return registrationNumberMatch; }
    public void setRegistrationNumberMatch(Boolean registrationNumberMatch) { this.registrationNumberMatch = registrationNumberMatch; }

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

    public String getOfficialVerification() { return officialVerification; }
    public void setOfficialVerification(String officialVerification) { this.officialVerification = officialVerification; }

    public Boolean getTamperingIndicators() { return tamperingIndicators; }
    public void setTamperingIndicators(Boolean tamperingIndicators) { this.tamperingIndicators = tamperingIndicators; }

    public String getOcrStatus() { return ocrStatus; }
    public void setOcrStatus(String ocrStatus) { this.ocrStatus = ocrStatus; }

    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }

    public String getVerificationMode() { return verificationMode != null ? verificationMode : "PRODUCTION"; }
    public void setVerificationMode(String verificationMode) { this.verificationMode = verificationMode; }

    public boolean isDemo() { return isDemo; }
    public void setDemo(boolean demo) { isDemo = demo; }

    public String getDemoNotice() { return demoNotice; }
    public void setDemoNotice(String demoNotice) { this.demoNotice = demoNotice; }

    public java.util.Map<String, Object> getDemoVerificationReport() { return demoVerificationReport; }
    public void setDemoVerificationReport(java.util.Map<String, Object> demoVerificationReport) { this.demoVerificationReport = demoVerificationReport; }
}
