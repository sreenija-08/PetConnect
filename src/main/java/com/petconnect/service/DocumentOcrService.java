package com.petconnect.service;

import com.petconnect.dto.DocumentOcrResult;
import java.nio.file.Path;

/**
 * Clean service abstraction for document OCR and text extraction.
 * Allows underlying OCR providers (local, cloud, AI microservice) to be replaced cleanly.
 */
public interface DocumentOcrService {
    DocumentOcrResult extractText(Path filePath, String fileType);
}
