package com.petconnect.service;

import com.petconnect.dto.ShelterDocumentAiResult;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterDocument;

public interface ShelterDocumentAiService {

    ShelterDocument processAndVerifyDocument(Shelter shelter, String documentPath, String originalFilename, Long fileSize);

    ShelterDocument processAndVerifyDocument(Shelter shelter, String documentPath, String originalFilename, Long fileSize, String verificationMode);

    ShelterDocumentAiResult callAiVerification(Shelter shelter, String documentPath);

    ShelterDocumentAiResult callAiVerification(Shelter shelter, String documentPath, String verificationMode);

    int getAutoRejectThreshold();

    int getLowRiskThreshold();

    void updateThresholds(int autoRejectThreshold, int lowRiskThreshold);
}
