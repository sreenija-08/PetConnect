package com.petconnect.controller;

import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

@Controller
public class ShelterDocumentController {

    private final ShelterRepository shelterRepository;
    private final UserService userService;
    private final com.petconnect.repository.ShelterDocumentRepository shelterDocumentRepository;

    @Autowired
    public ShelterDocumentController(ShelterRepository shelterRepository,
                                     UserService userService,
                                     com.petconnect.repository.ShelterDocumentRepository shelterDocumentRepository) {
        this.shelterRepository = shelterRepository;
        this.userService = userService;
        this.shelterDocumentRepository = shelterDocumentRepository;
    }

    @GetMapping({"/shelters/{shelterId}/documents/{docType}", "/shelter-documents/{shelterId}/{docType}"})
    public ResponseEntity<Resource> viewShelterDocument(
            @PathVariable Long shelterId,
            @PathVariable String docType,
            Principal principal,
            Authentication authentication) {

        if (principal == null || authentication == null) {
            throw new AccessDeniedException("Authentication required to access shelter verification documents.");
        }

        Shelter shelter = shelterRepository.findById(shelterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shelter not found"));

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        User currentUser = userService.findByEmail(principal.getName());
        boolean isOwner = shelter.getOwner() != null && currentUser != null &&
                shelter.getOwner().getId().equals(currentUser.getId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You do not have permission to access this verification document.");
        }

        String rawDocPath = null;
        if (docType.startsWith("id-") || docType.matches("\\d+")) {
            Long docId = Long.parseLong(docType.replace("id-", ""));
            com.petconnect.entity.ShelterDocument docRecord = shelterDocumentRepository.findById(docId).orElse(null);
            if (docRecord != null && docRecord.getShelter().getId().equals(shelterId)) {
                rawDocPath = docRecord.getDocumentUrl();
            }
        } else {
            switch (docType.toLowerCase()) {
                case "registration":
                case "reg":
                    rawDocPath = shelter.getRegistrationDocument();
                    break;
                case "identity":
                case "id":
                    rawDocPath = shelter.getIdentityDocument();
                    break;
                case "address":
                case "addr":
                    rawDocPath = shelter.getAddressProof();
                    break;
                case "other":
                    rawDocPath = shelter.getOtherDocument();
                    break;
                default:
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid document type: " + docType);
            }
        }

        if (rawDocPath == null || rawDocPath.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document has not been uploaded.");
        }

        // Clean path and resolve file on disk
        String cleaned = rawDocPath.replace('\\', '/');
        if (cleaned.startsWith("/")) {
            cleaned = cleaned.substring(1);
        }

        Path filePath = Paths.get(cleaned).normalize();
        if (!Files.exists(filePath)) {
            // Check fallback in uploads/shelter-documents/ or uploads/shelter-docs/
            String filename = Paths.get(cleaned).getFileName().toString();
            Path inDocs = Paths.get("uploads", "shelter-documents", filename).normalize();
            Path inOldDocs = Paths.get("uploads", "shelter-docs", filename).normalize();
            if (Files.exists(inDocs)) {
                filePath = inDocs;
            } else if (Files.exists(inOldDocs)) {
                filePath = inOldDocs;
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document file not found on disk.");
            }
        }

        // Security check: ensure path is within uploads directory
        Path baseUploads = Paths.get("uploads").toAbsolutePath().normalize();
        if (!filePath.toAbsolutePath().normalize().startsWith(baseUploads)) {
            throw new AccessDeniedException("Access to path outside upload directory is forbidden.");
        }

        File file = filePath.toFile();
        Resource resource = new FileSystemResource(file);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String nameLower = file.getName().toLowerCase();
        if (nameLower.endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (nameLower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (nameLower.endsWith(".jpg") || nameLower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (nameLower.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getName() + "\"")
                .body(resource);
    }
}
