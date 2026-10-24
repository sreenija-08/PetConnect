package com.petconnect.dto;

import java.util.Map;

public class DocumentOcrResult {
    private boolean success;
    private String text;
    private String ocrStatus; // SUCCESS, NO_TEXT, OCR_FAILED
    private String errorMessage;
    private Map<String, String> extractedFields;

    public DocumentOcrResult() {}

    public DocumentOcrResult(boolean success, String text, String ocrStatus) {
        this.success = success;
        this.text = text;
        this.ocrStatus = ocrStatus;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getText() { return text != null ? text : ""; }
    public void setText(String text) { this.text = text; }

    public String getOcrStatus() { return ocrStatus != null ? ocrStatus : "OCR_FAILED"; }
    public void setOcrStatus(String ocrStatus) { this.ocrStatus = ocrStatus; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Map<String, String> getExtractedFields() { return extractedFields; }
    public void setExtractedFields(Map<String, String> extractedFields) { this.extractedFields = extractedFields; }
}
