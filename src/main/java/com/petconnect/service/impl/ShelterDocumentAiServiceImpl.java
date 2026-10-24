package com.petconnect.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.petconnect.dto.ShelterDocumentAiResult;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterDocument;
import com.petconnect.entity.ShelterVerificationAudit;
import com.petconnect.entity.User;
import com.petconnect.repository.ShelterDocumentRepository;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.repository.ShelterVerificationAuditRepository;
import com.petconnect.service.EmailService;
import com.petconnect.service.NotificationService;
import com.petconnect.service.ShelterDocumentAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShelterDocumentAiServiceImpl implements ShelterDocumentAiService {

    private static final Logger log = LoggerFactory.getLogger(ShelterDocumentAiServiceImpl.class);

    private final ShelterRepository shelterRepository;
    private final ShelterDocumentRepository shelterDocumentRepository;
    private final ShelterVerificationAuditRepository auditRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${document.verification.ai-service-url:http://127.0.0.1:8000/verify-document}")
    private String aiServiceUrl;

    @Value("${document.verification.auto-reject-threshold:40}")
    private int autoRejectThreshold;

    @Value("${document.verification.low-risk-threshold:85}")
    private int lowRiskThreshold;

    @Autowired
    public ShelterDocumentAiServiceImpl(ShelterRepository shelterRepository,
                                        ShelterDocumentRepository shelterDocumentRepository,
                                        ShelterVerificationAuditRepository auditRepository,
                                        NotificationService notificationService,
                                        EmailService emailService,
                                        RestTemplateBuilder restTemplateBuilder) {
        this.shelterRepository = shelterRepository;
        this.shelterDocumentRepository = shelterDocumentRepository;
        this.auditRepository = auditRepository;
        this.notificationService = notificationService;
        this.emailService = emailService;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(4000))
                .setReadTimeout(Duration.ofMillis(6000))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public int getAutoRejectThreshold() {
        return autoRejectThreshold;
    }

    @Override
    public int getLowRiskThreshold() {
        return lowRiskThreshold;
    }

    @Override
    public void updateThresholds(int autoRejectThreshold, int lowRiskThreshold) {
        if (autoRejectThreshold >= 0 && autoRejectThreshold < lowRiskThreshold && lowRiskThreshold <= 100) {
            this.autoRejectThreshold = autoRejectThreshold;
            this.lowRiskThreshold = lowRiskThreshold;
            log.info("Updated AI verification thresholds: autoRejectThreshold={}, lowRiskThreshold={}",
                    autoRejectThreshold, lowRiskThreshold);
        } else {
            throw new IllegalArgumentException("Invalid threshold values. Must satisfy 0 <= autoRejectThreshold < lowRiskThreshold <= 100");
        }
    }

    @Override
    @Transactional
    public ShelterDocument processAndVerifyDocument(Shelter shelter, String documentPath, String originalFilename, Long fileSize) {
        return processAndVerifyDocument(shelter, documentPath, originalFilename, fileSize, shelter.getVerificationMode());
    }

    @Override
    @Transactional
    public ShelterDocument processAndVerifyDocument(Shelter shelter, String documentPath, String originalFilename, Long fileSize, String verificationMode) {
        if (shelter == null || documentPath == null) {
            throw new IllegalArgumentException("Shelter and documentPath are required");
        }

        String mode = (verificationMode != null && "DEMO".equalsIgnoreCase(verificationMode)) ? "DEMO" : "PRODUCTION";
        boolean isDemo = "DEMO".equalsIgnoreCase(mode);

        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".")).toUpperCase().replace(".", "");
        } else if (documentPath.contains(".")) {
            ext = documentPath.substring(documentPath.lastIndexOf(".")).toUpperCase().replace(".", "");
        }

        // 1. Create document entity
        ShelterDocument doc = new ShelterDocument(shelter, documentPath, originalFilename, ext, fileSize);
        doc.setVerificationMode(mode);
        doc.setVerificationStatus("PROCESSING");

        // 2. Call AI Verification Service
        ShelterDocumentAiResult aiResult = callAiVerification(shelter, documentPath, mode);

        // 3. Apply results to document record
        doc.setDocumentType(aiResult.getDocumentType());
        doc.setVerificationConfidenceScore(aiResult.getConfidenceScore());
        doc.setRiskLevel(aiResult.getRiskLevel());
        doc.setVerificationStatus(aiResult.getVerificationStatus());
        doc.setAutoRejected(aiResult.isAutoRejected());
        doc.setHardFail(aiResult.isHardFail());
        doc.setRejectionReasonCode(aiResult.getRejectionReasonCode());
        doc.setRejectionReason(aiResult.getRejectionReason());

        doc.setNameMatch(aiResult.getNameMatch());
        doc.setAddressMatch(aiResult.getAddressMatch());
        doc.setRegistrationNumberDetected(aiResult.getRegistrationNumberDetected());
        doc.setRegistrationNumberMatch(aiResult.getRegistrationNumberMatch());
        doc.setExtractedRegistrationNumber(aiResult.getExtractedRegistrationNumber());

        doc.setDateValid(aiResult.getDateValid());
        doc.setIssueDate(aiResult.getIssueDate());
        doc.setExpiryDate(aiResult.getExpiryDate());

        doc.setQrDetected(aiResult.getQrDetected());
        doc.setQrVerified(aiResult.getQrVerified());
        doc.setOfficialVerificationStatus(aiResult.getOfficialVerification());

        doc.setTamperingIndicators(aiResult.getTamperingIndicators());
        doc.setOcrStatus(aiResult.getOcrStatus());

        try {
            doc.setVerificationReasons(objectMapper.writeValueAsString(aiResult.getReasons()));
            doc.setAiAnalysisJson(aiResult.getRawJson());
        } catch (Exception e) {
            doc.setVerificationReasons(String.join("; ", aiResult.getReasons()));
        }

        doc.setVerifiedAt(LocalDateTime.now());
        doc.setVerifiedBy("SYSTEM_AI");

        ShelterDocument savedDoc = shelterDocumentRepository.save(doc);

        // 4. Update parent Shelter entity
        shelter.setVerificationMode(mode);
        shelter.setVerificationStatus(aiResult.getVerificationStatus());
        shelter.setVerificationConfidenceScore(aiResult.getConfidenceScore());
        shelter.setRiskLevel(aiResult.getRiskLevel());
        shelter.setAutoRejected(aiResult.isAutoRejected());
        shelter.setHardFail(aiResult.isHardFail());
        shelter.setRejectionReasonCode(aiResult.getRejectionReasonCode());
        shelter.setRejectionReason(aiResult.getRejectionReason());
        shelter.setAiAnalysisJson(aiResult.getRawJson());
        try {
            shelter.setVerificationReasons(objectMapper.writeValueAsString(aiResult.getReasons()));
        } catch (Exception e) {
            shelter.setVerificationReasons(String.join("; ", aiResult.getReasons()));
        }

        if (aiResult.getExtractedRegistrationNumber() != null && !aiResult.getExtractedRegistrationNumber().isBlank()) {
            if (shelter.getRegistrationNumber() == null || shelter.getRegistrationNumber().isBlank()) {
                shelter.setRegistrationNumber(aiResult.getExtractedRegistrationNumber());
            }
        }

        shelterRepository.save(shelter);

        // 5. Create immutable audit record
        String decision = aiResult.getVerificationStatus();
        ShelterVerificationAudit audit = new ShelterVerificationAudit(
                shelter.getId(),
                savedDoc.getId(),
                aiResult.getConfidenceScore(),
                aiResult.getRiskLevel(),
                aiResult.isHardFail(),
                decision,
                aiResult.getRejectionReasonCode(),
                String.join(" | ", aiResult.getReasons()),
                "SYSTEM_AI",
                mode
        );
        auditRepository.save(audit);

        // 6. Notify Shelter Owner based on outcome
        notifyOwner(shelter, aiResult);

        return savedDoc;
    }

    @Override
    public ShelterDocumentAiResult callAiVerification(Shelter shelter, String documentPath) {
        return callAiVerification(shelter, documentPath, shelter.getVerificationMode());
    }

    @Override
    public ShelterDocumentAiResult callAiVerification(Shelter shelter, String documentPath, String verificationMode) {
        String mode = (verificationMode != null && "DEMO".equalsIgnoreCase(verificationMode)) ? "DEMO" : "PRODUCTION";
        // Resolve absolute file path on disk
        String cleaned = documentPath.replace('\\', '/');
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }
        Path path = Paths.get(cleaned).normalize();
        if (!path.toFile().exists()) {
            // Check in uploads/shelter-documents/
            String filename = path.getFileName().toString();
            Path inDocs = Paths.get("uploads", "shelter-documents", filename).normalize();
            if (inDocs.toFile().exists()) {
                path = inDocs;
            }
        }

        File file = path.toFile();
        if (!file.exists()) {
            ShelterDocumentAiResult missingResult = new ShelterDocumentAiResult();
            missingResult.setVerificationMode(mode);
            missingResult.setDemo("DEMO".equalsIgnoreCase(mode));
            missingResult.setDocumentType("UNKNOWN");
            missingResult.setConfidenceScore(0);
            missingResult.setRiskLevel("HIGH");
            missingResult.setHardFail(true);
            missingResult.setAutoRejected(true);
            missingResult.setVerificationStatus("DEMO".equalsIgnoreCase(mode) ? "DEMO_VALIDATION_FAILED" : "AUTO_REJECTED");
            missingResult.setRejectionReasonCode("FILE_NOT_FOUND");
            missingResult.setRejectionReason("Verification file does not exist on disk.");
            missingResult.getReasons().add("Uploaded file not found on server.");
            return missingResult;
        }

        // Prepare request body for FastAPI microservice
        Map<String, Object> req = new HashMap<>();
        req.put("filePath", file.getAbsolutePath());
        req.put("shelterName", shelter.getName() != null ? shelter.getName() : "");
        req.put("address", shelter.getAddress() != null ? shelter.getAddress() : (shelter.getLocation() != null ? shelter.getLocation() : ""));
        req.put("city", shelter.getCity() != null ? shelter.getCity() : "");
        req.put("state", shelter.getState() != null ? shelter.getState() : "");
        req.put("registrationNumber", shelter.getRegistrationNumber() != null ? shelter.getRegistrationNumber() : "");
        req.put("autoRejectThreshold", this.autoRejectThreshold);
        req.put("lowRiskThreshold", this.lowRiskThreshold);
        req.put("verificationMode", mode);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(aiServiceUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return parseAndValidateAiResponse(response.getBody(), mode);
            } else {
                log.warn("AI service returned non-2xx status: {}", response.getStatusCode());
                return fallbackManualReview("AI service returned unexpected status: " + response.getStatusCode(), mode);
            }
        } catch (Exception e) {
            log.error("Failed to connect to AI Verification Service at {}: {}. Falling back to MANUAL_REVIEW as per Section 28.",
                    aiServiceUrl, e.getMessage());
            return fallbackManualReview("AI verification service temporarily offline. Routed to Manual Review.", mode);
        }
    }

    private ShelterDocumentAiResult parseAndValidateAiResponse(String jsonString, String mode) {
        boolean isDemo = "DEMO".equalsIgnoreCase(mode);
        ShelterDocumentAiResult result = new ShelterDocumentAiResult();
        result.setRawJson(jsonString);
        result.setVerificationMode(mode);
        result.setDemo(isDemo);

        try {
            JsonNode root = objectMapper.readTree(jsonString);

            // Required fields & score range validation
            int score = root.has("confidenceScore") ? root.get("confidenceScore").asInt(0) : 0;
            if (score < 0 || score > 100) {
                log.error("Invalid AI confidence score: {}. Normalizing to valid range 0-100.", score);
                score = Math.max(0, Math.min(100, score));
            }
            result.setConfidenceScore(score);

            String docType = root.has("documentType") ? root.get("documentType").asText("UNKNOWN") : "UNKNOWN";
            result.setDocumentType(docType);

            String risk = root.has("riskLevel") ? root.get("riskLevel").asText("MEDIUM") : "MEDIUM";
            result.setRiskLevel(risk);

            boolean hardFail = root.has("hardFail") && root.get("hardFail").asBoolean(false);
            result.setHardFail(hardFail);

            boolean autoRejected = root.has("autoRejected") && root.get("autoRejected").asBoolean(false);

            String status;
            if (isDemo) {
                if (hardFail || score < autoRejectThreshold) {
                    status = "DEMO_VALIDATION_FAILED";
                    autoRejected = true;
                } else if (score < lowRiskThreshold) {
                    status = "DEMO_MANUAL_REVIEW";
                    autoRejected = false;
                } else {
                    status = "DEMO_VALIDATION_PASSED";
                    autoRejected = false;
                }
                result.setOfficialVerification("DEMO_TEST_DATA");
                result.setDemoNotice("DEMO MODE — FOR TESTING ONLY: Demo results do not prove government document authenticity.");
                if (root.has("demoVerificationReport") && !root.get("demoVerificationReport").isNull()) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> rep = objectMapper.convertValue(root.get("demoVerificationReport"), Map.class);
                        result.setDemoVerificationReport(rep);
                    } catch (Exception ignored) {}
                }
            } else {
                // Production Deterministic Backend Enforcement (Section 14 & 15: Score + Hard Fail)
                if (hardFail) {
                    status = "AUTO_REJECTED";
                    autoRejected = true;
                } else if (score < autoRejectThreshold) {
                    status = "AUTO_REJECTED";
                    autoRejected = true;
                } else if (score < lowRiskThreshold) {
                    status = "MANUAL_REVIEW";
                    autoRejected = false;
                } else {
                    status = "PENDING"; // Low Risk Admin Review
                    autoRejected = false;
                }
                result.setOfficialVerification(root.has("officialVerification") ? root.get("officialVerification").asText("NOT_AVAILABLE") : "NOT_AVAILABLE");
            }

            result.setVerificationStatus(status);
            result.setAutoRejected(autoRejected);

            if (root.has("rejectionReasonCode") && !root.get("rejectionReasonCode").isNull()) {
                result.setRejectionReasonCode(root.get("rejectionReasonCode").asText());
            }
            if (root.has("rejectionReason") && !root.get("rejectionReason").isNull()) {
                result.setRejectionReason(root.get("rejectionReason").asText());
            }

            if (root.has("nameMatch") && !root.get("nameMatch").isNull()) {
                result.setNameMatch(root.get("nameMatch").asBoolean());
            }
            if (root.has("addressMatch") && !root.get("addressMatch").isNull()) {
                result.setAddressMatch(root.get("addressMatch").asBoolean());
            }
            if (root.has("registrationNumberDetected") && !root.get("registrationNumberDetected").isNull()) {
                result.setRegistrationNumberDetected(root.get("registrationNumberDetected").asBoolean());
            }
            if (root.has("registrationNumberMatch") && !root.get("registrationNumberMatch").isNull()) {
                result.setRegistrationNumberMatch(root.get("registrationNumberMatch").asBoolean());
            }
            if (root.has("extractedRegistrationNumber") && !root.get("extractedRegistrationNumber").isNull()) {
                result.setExtractedRegistrationNumber(root.get("extractedRegistrationNumber").asText());
            }

            if (root.has("dateValid") && !root.get("dateValid").isNull()) {
                result.setDateValid(root.get("dateValid").asBoolean());
            }
            if (root.has("issueDate") && !root.get("issueDate").isNull()) {
                result.setIssueDate(root.get("issueDate").asText());
            }
            if (root.has("expiryDate") && !root.get("expiryDate").isNull()) {
                result.setExpiryDate(root.get("expiryDate").asText());
            }

            if (root.has("qrDetected") && !root.get("qrDetected").isNull()) {
                result.setQrDetected(root.get("qrDetected").asBoolean());
            }
            if (root.has("qrVerified") && !root.get("qrVerified").isNull()) {
                result.setQrVerified(root.get("qrVerified").asBoolean());
            }

            if (root.has("tamperingIndicators") && !root.get("tamperingIndicators").isNull()) {
                result.setTamperingIndicators(root.get("tamperingIndicators").asBoolean());
            }
            if (root.has("ocrStatus") && !root.get("ocrStatus").isNull()) {
                result.setOcrStatus(root.get("ocrStatus").asText());
            }

            if (root.has("reasons") && root.get("reasons").isArray()) {
                for (JsonNode reasonNode : root.get("reasons")) {
                    result.getReasons().add(reasonNode.asText());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse AI response JSON: {}", e.getMessage());
            return fallbackManualReview("JSON parsing error from AI service. Routed to Manual Review.", mode);
        }

        return result;
    }

    private ShelterDocumentAiResult fallbackManualReview(String reason, String mode) {
        boolean isDemo = "DEMO".equalsIgnoreCase(mode);
        ShelterDocumentAiResult fallback = new ShelterDocumentAiResult();
        fallback.setVerificationMode(mode);
        fallback.setDemo(isDemo);
        fallback.setDocumentType(isDemo ? "DEMO_SHELTER_REGISTRATION_CERTIFICATE" : "SHELTER_DOCUMENT");
        fallback.setConfidenceScore(isDemo ? 50 : 60);
        fallback.setRiskLevel("MEDIUM");
        fallback.setVerificationStatus(isDemo ? "DEMO_MANUAL_REVIEW" : "MANUAL_REVIEW");
        fallback.setAutoRejected(false);
        fallback.setHardFail(false);
        fallback.setOfficialVerification(isDemo ? "DEMO_TEST_DATA" : "NOT_AVAILABLE");
        fallback.setOcrStatus("PENDING_REVIEW");
        if (isDemo) {
            fallback.setDemoNotice("DEMO MODE — FOR TESTING ONLY: Demo results do not prove government document authenticity.");
            fallback.getReasons().add("Demo document could not be analyzed.");
            fallback.getReasons().add("You may retry analyzing or verify manually.");
        } else {
            fallback.getReasons().add("ℹ️ " + reason);
            fallback.getReasons().add("ℹ️ Administrator can manually inspect document details.");
        }
        return fallback;
    }

    private void notifyOwner(Shelter shelter, ShelterDocumentAiResult result) {
        User owner = shelter.getOwner();
        if (owner == null) return;

        if (result.isDemo() || "DEMO".equalsIgnoreCase(shelter.getVerificationMode())) {
            String title = "Demo Document Verification: " + result.getVerificationStatus();
            String content = "[DEMO MODE — FOR TESTING ONLY] Your demonstration document verification result is: " +
                    result.getVerificationStatus() + " (Score: " + result.getConfidenceScore() + "/100). " +
                    "This is a simulated test result and does not represent official government document authenticity.";
            notificationService.createNotification(owner, title, content, "SHELTER");
            return;
        }

        if (result.isAutoRejected() || "AUTO_REJECTED".equalsIgnoreCase(result.getVerificationStatus())) {
            String title = "Shelter Verification Document Auto-Rejected";
            String reason = (result.getRejectionReason() != null && !result.getRejectionReason().isBlank())
                    ? result.getRejectionReason()
                    : "The uploaded file did not meet required verification criteria.";
            String content = "Your shelter verification document was automatically rejected because it did not meet the required verification criteria (" + reason + "). Please upload a valid official certificate under My Shelter to re-verify.";

            notificationService.createNotification(owner, title, content, "SHELTER");
            try {
                emailService.sendShelterVerificationEmail(owner.getEmail(), shelter.getName(), title, content);
            } catch (Exception e) {
                log.warn("Failed to dispatch auto-rejection email to {}: {}", owner.getEmail(), e.getMessage());
            }
        } else if ("MANUAL_REVIEW".equalsIgnoreCase(result.getVerificationStatus())) {
            String title = "Shelter Document Under Manual Review";
            String content = "Your shelter verification document has been submitted and is currently queued for manual review by the platform administrator.";
            notificationService.createNotification(owner, title, content, "SHELTER");
        } else {
            String title = "Shelter Verification Document Received";
            String content = "Your shelter verification document has been analyzed by AI with high confidence (" +
                    result.getConfidenceScore() + "/100) and is awaiting administrator approval.";
            notificationService.createNotification(owner, title, content, "SHELTER");
        }
    }
}
