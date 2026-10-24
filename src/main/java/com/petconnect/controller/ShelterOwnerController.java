package com.petconnect.controller;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.Notification;
import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterImage;
import com.petconnect.entity.User;
import com.petconnect.repository.ShelterImageRepository;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.NotificationService;
import com.petconnect.service.PetService;
import com.petconnect.service.UserService;
import com.petconnect.entity.CareBooking;
import com.petconnect.service.CareBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import jakarta.servlet.http.HttpServletRequest;
import com.petconnect.repository.ShelterDocumentRepository;

@Controller
@RequestMapping("/owner")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('SHELTER_OWNER')")
public class ShelterOwnerController {

    private final UserService userService;
    private final AdoptionService adoptionService;
    private final NotificationService notificationService;
    private final PetService petService;
    private final ShelterRepository shelterRepository;
    private final CareBookingService careBookingService;
    private final ShelterImageRepository shelterImageRepository;
    private final com.petconnect.service.AdoptionMeetingService adoptionMeetingService;
    private final com.petconnect.service.ShelterDocumentAiService shelterDocumentAiService;
    private final ShelterDocumentRepository shelterDocumentRepository;

    @Autowired
    public ShelterOwnerController(UserService userService,
                                  AdoptionService adoptionService,
                                  NotificationService notificationService,
                                  PetService petService,
                                  ShelterRepository shelterRepository,
                                  CareBookingService careBookingService,
                                  ShelterImageRepository shelterImageRepository,
                                  com.petconnect.service.AdoptionMeetingService adoptionMeetingService,
                                  com.petconnect.service.ShelterDocumentAiService shelterDocumentAiService,
                                  ShelterDocumentRepository shelterDocumentRepository) {
        this.userService = userService;
        this.adoptionService = adoptionService;
        this.notificationService = notificationService;
        this.petService = petService;
        this.shelterRepository = shelterRepository;
        this.careBookingService = careBookingService;
        this.shelterImageRepository = shelterImageRepository;
        this.adoptionMeetingService = adoptionMeetingService;
        this.shelterDocumentAiService = shelterDocumentAiService;
        this.shelterDocumentRepository = shelterDocumentRepository;
    }

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            model.addAttribute("loggedInUser", user);
            if (user != null) {
                model.addAttribute("unreadNotificationsCount", notificationService.getUnreadCount(user));
            }
        }
    }

    /**
     * Resolves the shelter belonging to the currently logged-in shelter owner.
     * Ensures every active shelter owner always has a valid shelter in MySQL.
     */
    private Shelter getShelterForOwner(User user) {
        if (user == null) return null;
        List<Shelter> shelters = shelterRepository.findByOwner(user);
        if (!shelters.isEmpty()) {
            return shelters.get(0);
        }
        // If saranya@gmail.com, associate Happy Paws Shelter
        if (user.getEmail().equalsIgnoreCase("saranya@gmail.com")) {
            Shelter hp = shelterRepository.findByName("Happy Paws Shelter").orElse(null);
            if (hp != null) {
                hp.setOwner(user);
                return shelterRepository.save(hp);
            }
        }
        // If owner@petconnect.com, associate Safe Tails or Happy Paws
        if (user.getEmail().equalsIgnoreCase("owner@petconnect.com")) {
            Shelter st = shelterRepository.findByName("Safe Tails Shelter").orElse(null);
            if (st != null) {
                st.setOwner(user);
                return shelterRepository.save(st);
            }
        }
        // Auto-provision a verified shelter for any registered owner
        Shelter s = new Shelter(
                (user.getFullName() != null && !user.getFullName().isBlank()) ? user.getFullName() + "'s Shelter" : "Happy Paws Shelter",
                "Chennai, Tamil Nadu",
                "Rescue and animal welfare organization providing care, boarding, and adoption support.",
                20, 10, "PENDING", true,
                user.getPhone() != null ? user.getPhone() : "9876543210",
                user.getEmail(),
                user
        );
        return shelterRepository.save(s);
    }

    private String saveUploadedPetPhoto(MultipartFile file, Long shelterId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
        }
        String origFilename = file.getOriginalFilename();
        if (origFilename == null || !origFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file: missing file extension.");
        }
        String ext = origFilename.substring(origFilename.lastIndexOf(".")).toLowerCase();
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerCt = contentType.toLowerCase();
            if (!lowerCt.equals("image/jpeg") && !lowerCt.equals("image/png") && !lowerCt.equals("image/webp")) {
                throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
            }
        }
        try {
            Path uploadDir = Paths.get("uploads", "pets");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String filename = "pet-shelter-" + shelterId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
            Path destination = uploadDir.resolve(filename).normalize();
            if (!destination.startsWith(uploadDir.toAbsolutePath().normalize()) && !destination.startsWith(uploadDir.normalize())) {
                throw new SecurityException("Invalid file destination path.");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/pets/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store uploaded pet photo: " + e.getMessage(), e);
        }
    }

    private void deleteLocalPetPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return;
        if (photoUrl.startsWith("/uploads/pets/")) {
            try {
                String filename = photoUrl.substring("/uploads/pets/".length());
                Path filePath = Paths.get("uploads", "pets", filename).normalize();
                Path uploadDir = Paths.get("uploads", "pets").normalize();
                if (filePath.startsWith(uploadDir)) {
                    Files.deleteIfExists(filePath);
                }
            } catch (Exception e) {
                System.err.println("Could not delete local pet photo: " + e.getMessage());
            }
        }
    }

    private String saveUploadedShelterPhoto(MultipartFile file, Long shelterId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
        }
        String origFilename = file.getOriginalFilename();
        if (origFilename == null || !origFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file: missing file extension.");
        }
        String ext = origFilename.substring(origFilename.lastIndexOf(".")).toLowerCase();
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerCt = contentType.toLowerCase();
            if (!lowerCt.equals("image/jpeg") && !lowerCt.equals("image/png") && !lowerCt.equals("image/webp")) {
                throw new IllegalArgumentException("Please upload a valid JPG, PNG, or WEBP image under 5 MB.");
            }
        }
        try {
            Path uploadDir = Paths.get("uploads", "shelters");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String filename = "shelter-" + shelterId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
            Path destination = uploadDir.resolve(filename).normalize();
            if (!destination.startsWith(uploadDir.toAbsolutePath().normalize()) && !destination.startsWith(uploadDir.normalize())) {
                throw new SecurityException("Invalid file destination path.");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/shelters/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store uploaded shelter photo: " + e.getMessage(), e);
        }
    }

    private void deleteLocalShelterPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) return;
        if (photoUrl.startsWith("/uploads/shelters/")) {
            try {
                String filename = photoUrl.substring("/uploads/shelters/".length());
                Path filePath = Paths.get("uploads", "shelters", filename).normalize();
                Path uploadDir = Paths.get("uploads", "shelters").normalize();
                if (filePath.startsWith(uploadDir)) {
                    Files.deleteIfExists(filePath);
                }
            } catch (Exception e) {
                System.err.println("Could not delete local shelter photo: " + e.getMessage());
            }
        }
    }

    private String saveUploadedDocument(MultipartFile file, String prefix, Long shelterId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Document size exceeds 10MB limit.");
        }
        String origFilename = file.getOriginalFilename();
        if (origFilename == null || !origFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file: missing file extension.");
        }
        String ext = origFilename.substring(origFilename.lastIndexOf(".")).toLowerCase();
        if (!ext.equals(".pdf") && !ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IllegalArgumentException("Unsupported document format. Allowed: PDF, JPG, JPEG, PNG, WEBP.");
        }
        try {
            Path uploadDir = Paths.get("uploads", "shelter-documents");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String safePrefix = prefix.replaceAll("[^a-zA-Z0-9_-]", "");
            String filename = "shelter-" + shelterId + "-" + safePrefix + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path destination = uploadDir.resolve(filename).normalize();
            if (!destination.startsWith(uploadDir.toAbsolutePath().normalize()) && !destination.startsWith(uploadDir.normalize())) {
                throw new SecurityException("Invalid file destination path.");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/shelter-documents/" + filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        int appCount = 0;
        int petCount = 0;
        int bookingsCount = 0;
        int staysCount = 0;
        List<AdoptionApplication> recentApplications = Collections.emptyList();
        List<Notification> recentNotifications = Collections.emptyList();
        Shelter shelter = null;

        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                shelter = getShelterForOwner(user);
                if (shelter != null) {
                    List<AdoptionApplication> apps = adoptionService.getApplicationsByShelter(shelter);
                    appCount = apps.size();
                    recentApplications = apps.stream().limit(5).collect(Collectors.toList());
                    petCount = petService.getPetsByShelter(shelter).size();
                    bookingsCount = careBookingService.getShelterRequests(shelter).size();
                    staysCount = careBookingService.getCurrentStays(shelter).size();
                }
                recentNotifications = notificationService.getUserNotifications(user).stream().limit(5).collect(Collectors.toList());
            }
        }
        model.addAttribute("shelter", shelter);
        model.addAttribute("petsCount", petCount);
        model.addAttribute("adoptionsCount", appCount);
        model.addAttribute("bookingsCount", bookingsCount);
        model.addAttribute("staysCount", staysCount);
        model.addAttribute("recentApplications", recentApplications);
        model.addAttribute("recentNotifications", recentNotifications);
        return "owner/dashboard";
    }

    @GetMapping("/my-shelter")
    public String myShelter(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        model.addAttribute("shelter", shelter);
        int availableToday = (shelter != null) ? careBookingService.calculateAvailableCapacity(shelter, LocalDate.now(), LocalDate.now()) : 0;
        model.addAttribute("availableToday", availableToday);

        com.petconnect.entity.ShelterDocument latestDoc = (shelter != null)
                ? shelterDocumentRepository.findFirstByShelterOrderByCreatedAtDesc(shelter).orElse(null)
                : null;
        model.addAttribute("latestDoc", latestDoc);
        return "owner/my-shelter";
    }

    @PostMapping("/my-shelter")
    public String updateMyShelter(@ModelAttribute Shelter updatedShelter,
                                  @RequestParam(value = "registrationNumber", required = false) String registrationNumber,
                                  @RequestParam(value = "verificationMode", required = false, defaultValue = "PRODUCTION") String verificationMode,
                                  @RequestParam(value = "registrationDocFile", required = false) MultipartFile registrationDocFile,
                                  @RequestParam(value = "identityDocFile", required = false) MultipartFile identityDocFile,
                                  @RequestParam(value = "addressProofFile", required = false) MultipartFile addressProofFile,
                                  @RequestParam(value = "otherDocFile", required = false) MultipartFile otherDocFile,
                                  @RequestParam(value = "shelterPhotos", required = false) MultipartFile[] shelterPhotos,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter != null) {
            String mode = (verificationMode != null && "DEMO".equalsIgnoreCase(verificationMode)) ? "DEMO" : "PRODUCTION";
            shelter.setVerificationMode(mode);

            shelter.setName(updatedShelter.getName());
            shelter.setLocation(updatedShelter.getLocation());
            shelter.setDescription(updatedShelter.getDescription());
            shelter.setPhone(updatedShelter.getPhone());
            shelter.setEmail(updatedShelter.getEmail());
            shelter.setCapacity(updatedShelter.getCapacity());
            shelter.setAvailableCapacity(updatedShelter.getAvailableCapacity());
            shelter.setTemporaryCareEnabled(updatedShelter.isTemporaryCareEnabled());
            if (updatedShelter.getBoardingFee() != null && updatedShelter.getBoardingFee() > 0) {
                shelter.setBoardingFee(updatedShelter.getBoardingFee());
            }
            if (updatedShelter.getSupportedPetTypes() != null && !updatedShelter.getSupportedPetTypes().isBlank()) {
                shelter.setSupportedPetTypes(updatedShelter.getSupportedPetTypes());
            }

            if (updatedShelter.getAddress() != null) shelter.setAddress(updatedShelter.getAddress());
            if (updatedShelter.getArea() != null) shelter.setArea(updatedShelter.getArea());
            if (updatedShelter.getCity() != null) shelter.setCity(updatedShelter.getCity());
            if (updatedShelter.getState() != null) shelter.setState(updatedShelter.getState());

            if (registrationNumber != null && !registrationNumber.isBlank()) {
                shelter.setRegistrationNumber(registrationNumber.trim());
            }

            // Handle verification document uploads
            com.petconnect.entity.ShelterDocument verifiedDoc = null;
            if (registrationDocFile != null && !registrationDocFile.isEmpty()) {
                String regDoc = saveUploadedDocument(registrationDocFile, "reg", shelter.getId());
                if (regDoc != null) {
                    shelter.setRegistrationDocument(regDoc);
                    // Trigger AI Document Verification with verificationMode
                    verifiedDoc = shelterDocumentAiService.processAndVerifyDocument(
                            shelter, regDoc, registrationDocFile.getOriginalFilename(), registrationDocFile.getSize(), mode
                    );
                }
            }

            String idDoc = saveUploadedDocument(identityDocFile, "id", shelter.getId());
            if (idDoc != null) shelter.setIdentityDocument(idDoc);

            String addrDoc = saveUploadedDocument(addressProofFile, "addr", shelter.getId());
            if (addrDoc != null) shelter.setAddressProof(addrDoc);

            String othDoc = saveUploadedDocument(otherDocFile, "other", shelter.getId());
            if (othDoc != null) shelter.setOtherDocument(othDoc);

            // Handle shelter photos upload (up to 5 max)
            if (shelterPhotos != null && shelterPhotos.length > 0) {
                List<ShelterImage> existingImages = shelterImageRepository.findByShelterOrderByIdAsc(shelter);
                int currentCount = existingImages.size();
                for (MultipartFile photo : shelterPhotos) {
                    if (photo != null && !photo.isEmpty()) {
                        if (currentCount >= 5) {
                            redirectAttributes.addFlashAttribute("errorMessage", "Maximum of 5 photos allowed per shelter.");
                            break;
                        }
                        try {
                            String photoUrl = saveUploadedShelterPhoto(photo, shelter.getId());
                            ShelterImage img = new ShelterImage(shelter, photoUrl);
                            shelterImageRepository.save(img);
                            currentCount++;
                        } catch (Exception e) {
                            redirectAttributes.addFlashAttribute("errorMessage", "Shelter photo upload failed: " + e.getMessage());
                        }
                    }
                }
            }

            // If shelter was rejected and owner updates profile/uploads documents without a new reg doc
            if (verifiedDoc == null && "REJECTED".equalsIgnoreCase(shelter.getVerificationStatus())) {
                shelter.setVerificationStatus("PENDING");
                shelter.setRejectionReason(null);
                User admin = userService.findByEmail("admin@petconnect.com");
                if (admin != null) {
                    notificationService.createNotification(
                            admin,
                            "Shelter Re-Verification Requested",
                            "Shelter '" + shelter.getName() + "' updated profile/documents and requested re-verification.",
                            "SHELTER"
                    );
                }
            }

            shelterRepository.save(shelter);

            if (verifiedDoc != null) {
                if ("DEMO".equalsIgnoreCase(verifiedDoc.getVerificationMode())) {
                    if ("DEMO_VALIDATION_PASSED".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                        redirectAttributes.addFlashAttribute("successMessage",
                                "🧪 [DEMO MODE — FOR TESTING ONLY] Demo certificate analyzed successfully! Status: DEMO VALIDATION PASSED (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100). Simulated test verification passed.");
                    } else if ("DEMO_MANUAL_REVIEW".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                        redirectAttributes.addFlashAttribute("warningMessage",
                                "🧪 [DEMO MODE — FOR TESTING ONLY] Some required information could not be confidently extracted. Status: DEMO MANUAL REVIEW (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100). Manual review is required.");
                    } else {
                        String reasonText = verifiedDoc.getRejectionReason() != null ? verifiedDoc.getRejectionReason() : "Demo validation checks failed";
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "🧪 [DEMO MODE — FOR TESTING ONLY] " + reasonText + " Status: DEMO VALIDATION FAILED (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100).");
                    }
                } else {
                    if (verifiedDoc.isAutoRejected() || "AUTO_REJECTED".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                        String reasonText = verifiedDoc.getRejectionReason() != null ? verifiedDoc.getRejectionReason() : "Did not meet required criteria";
                        redirectAttributes.addFlashAttribute("errorMessage",
                                "AI Verification Result: Your certificate was automatically rejected (" + reasonText + "). Confidence Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100. Please upload an official valid registration certificate.");
                    } else if ("MANUAL_REVIEW".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                        redirectAttributes.addFlashAttribute("warningMessage",
                                "Document uploaded successfully! AI Verification Confidence is " + verifiedDoc.getVerificationConfidenceScore() + "/100 (Medium Risk). Your application has been queued for manual administrator review.");
                    } else {
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Document uploaded successfully! AI Verification Confidence is high (" + verifiedDoc.getVerificationConfidenceScore() + "/100, Low Risk). Awaiting final administrator review.");
                    }
                }
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Shelter profile, photos, and verification documents updated successfully!");
            }
        }
        return "redirect:/owner/my-shelter";
    }

    @PostMapping("/my-shelter/demo-scenario")
    public String runDemoScenario(@RequestParam("scenario") String scenario,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter not found.");
            return "redirect:/owner/my-shelter";
        }

        try {
            ensureDemoCertificatesExist();

            shelter.setVerificationMode("DEMO");
            String certFileName;
            String desc;

            if ("B".equalsIgnoreCase(scenario)) {
                // Scenario B: Uncertain Demo Document (Missing address, unclear reg number DEMO/2026/???)
                shelter.setName("Happy Tails Demo Shelter");
                shelter.setRegistrationNumber("DEMO/2026/001");
                shelter.setAddress("Hyderabad, Telangana");
                shelter.setCity("Hyderabad");
                shelter.setState("Telangana");
                shelter.setLocation("Hyderabad, Telangana");
                certFileName = "demo_certificate_uncertain_B.pdf";
                desc = "Scenario B (Uncertain Demo Document: Unclear registration number & missing address)";
            } else if ("C".equalsIgnoreCase(scenario)) {
                // Scenario C: Invalid Demo Document (Registration mismatch: DB has DEMO/2026/001, doc has DEMO/2026/999)
                shelter.setName("Happy Tails Demo Shelter");
                shelter.setRegistrationNumber("DEMO/2026/001");
                shelter.setAddress("Hyderabad, Telangana");
                shelter.setCity("Hyderabad");
                shelter.setState("Telangana");
                shelter.setLocation("Hyderabad, Telangana");
                certFileName = "demo_certificate_invalid_C.pdf";
                desc = "Scenario C (Invalid Demo Document: Registration number mismatch detected)";
            } else {
                // Scenario A: Valid Demo Document (Happy Tails Demo Shelter, DEMO/2026/001, Hyderabad, Telangana)
                shelter.setName("Happy Tails Demo Shelter");
                shelter.setRegistrationNumber("DEMO/2026/001");
                shelter.setAddress("Hyderabad, Telangana");
                shelter.setCity("Hyderabad");
                shelter.setState("Telangana");
                shelter.setLocation("Hyderabad, Telangana");
                certFileName = "demo_certificate_valid_A.pdf";
                desc = "Scenario A (Valid Demo Document: Full match for Happy Tails Demo Shelter)";
            }

            Path certPath = Paths.get("uploads", "shelter-documents", certFileName);
            long fileSize = Files.exists(certPath) ? Files.size(certPath) : 1024L;
            String relativeUrl = "/uploads/shelter-documents/" + certFileName;
            shelter.setRegistrationDocument(relativeUrl);
            shelterRepository.save(shelter);

            com.petconnect.entity.ShelterDocument verifiedDoc = shelterDocumentAiService.processAndVerifyDocument(
                    shelter, relativeUrl, certFileName, fileSize, "DEMO"
            );

            if ("DEMO_VALIDATION_PASSED".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "🧪 [DEMO " + desc + "] Result: DEMO VALIDATION PASSED (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100). Simulated test verification passed!");
            } else if ("DEMO_MANUAL_REVIEW".equalsIgnoreCase(verifiedDoc.getVerificationStatus())) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "🧪 [DEMO " + desc + "] Result: DEMO MANUAL REVIEW (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100). Some required information could not be confidently extracted. Manual review is required.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "🧪 [DEMO " + desc + "] Result: DEMO VALIDATION FAILED (Score: " + verifiedDoc.getVerificationConfidenceScore() + "/100). " +
                        (verifiedDoc.getRejectionReason() != null ? verifiedDoc.getRejectionReason() : "Registration number mismatch detected."));
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to run demo scenario: " + e.getMessage());
        }

        return "redirect:/owner/my-shelter";
    }

    private void ensureDemoCertificatesExist() {
        Path dir = Paths.get("uploads", "shelter-documents");
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            Path a = dir.resolve("demo_certificate_valid_A.pdf");
            Path b = dir.resolve("demo_certificate_uncertain_B.pdf");
            Path c = dir.resolve("demo_certificate_invalid_C.pdf");
            if (!Files.exists(a) || !Files.exists(b) || !Files.exists(c)) {
                ProcessBuilder pb = new ProcessBuilder("python", "ai_service/generate_demo_certificates.py");
                pb.directory(new File("."));
                Process p = pb.start();
                p.waitFor();
            }
        } catch (Exception ignored) {}
    }

    @PostMapping("/shelter-images/{imageId}/delete")
    public String deleteShelterImage(@PathVariable Long imageId,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        ShelterImage img = shelterImageRepository.findById(imageId).orElse(null);
        if (img != null && shelter != null && img.getShelter().getId().equals(shelter.getId())) {
            deleteLocalShelterPhoto(img.getImageUrl());
            shelterImageRepository.delete(img);
            redirectAttributes.addFlashAttribute("successMessage", "Shelter photo removed successfully.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Image not found or access denied.");
        }
        return "redirect:/owner/my-shelter";
    }

    @GetMapping("/my-pets")
    public String myPets(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        List<Pet> shelterPets = petService.getPetsByShelter(shelter);
        model.addAttribute("pets", shelterPets);
        model.addAttribute("shelter", shelter);
        model.addAttribute("isShelterApproved", shelter != null && "APPROVED".equalsIgnoreCase(shelter.getVerificationStatus()));
        return "owner/my-pets";
    }

    @GetMapping("/add-pet")
    public String addPetForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter == null || !"APPROVED".equalsIgnoreCase(shelter.getVerificationStatus())) {
            String currentStatus = shelter != null ? shelter.getVerificationStatus() : "NOT APPROVED";
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Shelter verification is currently " + currentStatus + 
                ". You can only add rescued pets after your shelter has been verified and approved by the platform administrator.");
            return "redirect:/owner/my-shelter";
        }
        Pet pet = new Pet();
        pet.setType("DOG");
        model.addAttribute("pet", pet);
        model.addAttribute("shelter", shelter);
        return "owner/add-pet";
    }

    @PostMapping("/add-pet")
    public String addPetSubmit(@ModelAttribute Pet pet,
                               @RequestParam(value = "petPhoto", required = false) MultipartFile petPhoto,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter == null || !"APPROVED".equalsIgnoreCase(shelter.getVerificationStatus())) {
            String currentStatus = shelter != null ? shelter.getVerificationStatus() : "NOT APPROVED";
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Action prohibited: Shelter is currently " + currentStatus + 
                ". Only verified & approved shelters can register pets for adoption.");
            return "redirect:/owner/my-shelter";
        }

        pet.setShelter(shelter);
        pet.setOwnerName(user.getFullName());
        pet.setVerificationStatus("PENDING");
        pet.setAdoptionStatus("AVAILABLE");

        // Handle uploaded pet photo
        if (petPhoto != null && !petPhoto.isEmpty()) {
            try {
                String photoUrl = saveUploadedPetPhoto(petPhoto, shelter.getId());
                pet.setPhotoUrl(photoUrl);
                pet.setImagePath(photoUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
                return "redirect:/owner/add-pet";
            }
        } else if (pet.getImagePath() == null || pet.getImagePath().isBlank()) {
            String defaultImg = "DOG".equalsIgnoreCase(pet.getType()) ?
                    "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=600" :
                    "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=600";
            pet.setImagePath(defaultImg);
            pet.setPhotoUrl(defaultImg);
        } else {
            pet.setPhotoUrl(pet.getImagePath());
        }

        petService.savePet(pet);
        redirectAttributes.addFlashAttribute("successMessage", "Pet '" + pet.getName() + "' added successfully! Status is PENDING admin verification.");
        return "redirect:/owner/my-pets";
    }

    @GetMapping("/edit-pet/{id}")
    public String editPetForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Pet pet = petService.getPetById(id);
        if (pet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
            return "redirect:/owner/my-pets";
        }
        Shelter shelter = getShelterForOwner(user);
        if (pet.getShelter() != null && shelter != null && !pet.getShelter().getId().equals(shelter.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit pets belonging to your shelter.");
            return "redirect:/owner/my-pets";
        }
        model.addAttribute("pet", pet);
        model.addAttribute("shelter", shelter);
        return "owner/edit-pet";
    }

    @PostMapping("/edit-pet/{id}")
    public String editPetSubmit(@PathVariable Long id,
                                @ModelAttribute Pet updatedPet,
                                @RequestParam(value = "petPhoto", required = false) MultipartFile petPhoto,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Pet existingPet = petService.getPetById(id);
        if (existingPet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
            return "redirect:/owner/my-pets";
        }
        Shelter shelter = getShelterForOwner(user);
        if (existingPet.getShelter() != null && shelter != null && !existingPet.getShelter().getId().equals(shelter.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You can only edit pets belonging to your shelter.");
            return "redirect:/owner/my-pets";
        }

        try {
            if (petPhoto != null && !petPhoto.isEmpty()) {
                deleteLocalPetPhoto(existingPet.getPhotoUrl());
                String photoUrl = saveUploadedPetPhoto(petPhoto, shelter != null ? shelter.getId() : 0L);
                updatedPet.setPhotoUrl(photoUrl);
                updatedPet.setImagePath(photoUrl);
            } else {
                updatedPet.setPhotoUrl(existingPet.getPhotoUrl());
                if (updatedPet.getImagePath() == null || updatedPet.getImagePath().isBlank()) {
                    updatedPet.setImagePath(existingPet.getImagePath());
                }
            }
            petService.updatePet(id, updatedPet, user);
            redirectAttributes.addFlashAttribute("successMessage", "Pet updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/my-pets";
    }

    @PostMapping("/delete-pet/{id}")
    public String deletePetSubmit(@PathVariable Long id,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            String result = petService.deletePet(id, user);
            if ("DELETED".equals(result)) {
                redirectAttributes.addFlashAttribute("successMessage", "Pet deleted successfully.");
            } else if ("DEACTIVATED".equals(result)) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "This pet has existing records and cannot be permanently deleted. It has been removed from public listings instead.");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting pet: " + e.getMessage());
        }
        return "redirect:/owner/my-pets";
    }

    // REAL SHELTER ADOPTION REQUESTS FROM MYSQL
    @GetMapping("/adoption-requests")
    public String adoptionRequests(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        List<AdoptionApplication> applications = adoptionService.getApplicationsByShelter(shelter);

        java.util.Map<Long, com.petconnect.entity.AdoptionMeeting> activeMeetingsByAppId = new java.util.HashMap<>();
        for (AdoptionApplication app : applications) {
            adoptionMeetingService.getActiveMeetingForApplication(app.getId())
                    .ifPresent(meeting -> activeMeetingsByAppId.put(app.getId(), meeting));
        }

        model.addAttribute("applications", applications);
        model.addAttribute("shelter", shelter);
        model.addAttribute("activeMeetingsByAppId", activeMeetingsByAppId);
        return "owner/adoption-requests";
    }

    // VIEW DETAILED APPLICATION FOR SHELTER OWNER
    @GetMapping("/adoption-requests/{id}")
    public String viewApplicationDetails(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        AdoptionApplication application = adoptionService.getApplicationById(id);
        if (application == null || application.getShelter() == null || shelter == null || !application.getShelter().getId().equals(shelter.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: You do not have permission to view adoption applications for another shelter.");
            return "redirect:/owner/adoption-requests";
        }
        model.addAttribute("application", application);
        model.addAttribute("adoptionApplication", application);
        model.addAttribute("app", application);
        model.addAttribute("pet", application.getPet());
        model.addAttribute("shelter", application.getShelter());
        model.addAttribute("availableSlots", adoptionService.getAvailableSlotsForApplication(id));
        List<com.petconnect.entity.ShelterVisitSlot> allSlots = adoptionService.getSlotsForApplication(id);
        model.addAttribute("allSlots", allSlots);

        // Current active/requested slot if any
        com.petconnect.entity.ShelterVisitSlot currentSlot = null;
        for (com.petconnect.entity.ShelterVisitSlot s : allSlots) {
            if (s.getApplication() != null && s.getApplication().getId().equals(id) &&
               ("REQUESTED".equalsIgnoreCase(s.getStatus()) || "CONFIRMED".equalsIgnoreCase(s.getStatus()) || "COMPLETED".equalsIgnoreCase(s.getStatus()))) {
                currentSlot = s;
                break;
            }
        }
        model.addAttribute("currentSlot", currentSlot);

        // Video Consultation Meeting Details
        java.util.Optional<com.petconnect.entity.AdoptionMeeting> activeMeeting = adoptionMeetingService.getActiveMeetingForApplication(id);
        model.addAttribute("activeMeeting", activeMeeting.orElse(null));
        List<com.petconnect.entity.AdoptionMeeting> meetingHistory = adoptionMeetingService.getMeetingsForApplication(id);
        model.addAttribute("meetingHistory", meetingHistory);

        return "owner/adoption-details";
    }

    // ACTION: SCHEDULE VIDEO CONSULTATION
    @PostMapping("/adoption-requests/{id}/schedule-meeting")
    public String scheduleVideoMeeting(@PathVariable Long id,
                                       @RequestParam("meetingDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate meetingDate,
                                       @RequestParam("meetingTime") String meetingTimeStr,
                                       @RequestParam(value = "durationMinutes", required = false, defaultValue = "30") Integer durationMinutes,
                                       @RequestParam(value = "notes", required = false, defaultValue = "") String notes,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            java.time.LocalTime meetingTime;
            if (meetingTimeStr.contains(":")) {
                String[] parts = meetingTimeStr.trim().split(":");
                int hours = Integer.parseInt(parts[0].trim());
                int mins = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                if (meetingTimeStr.toUpperCase().contains("PM") && hours < 12) hours += 12;
                if (meetingTimeStr.toUpperCase().contains("AM") && hours == 12) hours = 0;
                meetingTime = java.time.LocalTime.of(hours, mins);
            } else {
                meetingTime = java.time.LocalTime.parse(meetingTimeStr.trim());
            }

            adoptionMeetingService.scheduleMeeting(id, meetingDate, meetingTime, durationMinutes, notes, user);
            redirectAttributes.addFlashAttribute("successMessage", "📹 Video consultation meeting scheduled successfully! Meeting details and link have been sent to the applicant.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to schedule video meeting: " + e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: RESCHEDULE VIDEO CONSULTATION
    @PostMapping("/adoption-requests/{id}/reschedule-meeting")
    public String rescheduleVideoMeeting(@PathVariable Long id,
                                         @RequestParam("meetingId") Long meetingId,
                                         @RequestParam("meetingDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate meetingDate,
                                         @RequestParam("meetingTime") String meetingTimeStr,
                                         @RequestParam(value = "durationMinutes", required = false, defaultValue = "30") Integer durationMinutes,
                                         @RequestParam(value = "notes", required = false, defaultValue = "") String notes,
                                         Principal principal,
                                         RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            java.time.LocalTime meetingTime;
            if (meetingTimeStr.contains(":")) {
                String[] parts = meetingTimeStr.trim().split(":");
                int hours = Integer.parseInt(parts[0].trim());
                int mins = Integer.parseInt(parts[1].trim().replaceAll("[^0-9]", ""));
                if (meetingTimeStr.toUpperCase().contains("PM") && hours < 12) hours += 12;
                if (meetingTimeStr.toUpperCase().contains("AM") && hours == 12) hours = 0;
                meetingTime = java.time.LocalTime.of(hours, mins);
            } else {
                meetingTime = java.time.LocalTime.parse(meetingTimeStr.trim());
            }

            adoptionMeetingService.rescheduleMeeting(meetingId, meetingDate, meetingTime, durationMinutes, notes, user);
            redirectAttributes.addFlashAttribute("successMessage", "🔄 Video consultation meeting rescheduled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to reschedule video meeting: " + e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: CANCEL VIDEO CONSULTATION
    @PostMapping("/adoption-requests/{id}/cancel-meeting")
    public String cancelVideoMeeting(@PathVariable Long id,
                                     @RequestParam("meetingId") Long meetingId,
                                     @RequestParam(value = "reason", required = false, defaultValue = "") String reason,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionMeetingService.cancelMeeting(meetingId, reason, user);
            redirectAttributes.addFlashAttribute("successMessage", "Video consultation cancelled. Applicant has been notified.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to cancel meeting: " + e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: SHORTLIST
    @PostMapping("/adoption-requests/{id}/shortlist")
    public String shortlistApplication(@PathVariable Long id,
                                       @RequestParam(required = false, defaultValue = "") String remarks,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.shortlistApplication(id, remarks, user);
            redirectAttributes.addFlashAttribute("successMessage", "Application shortlisted! You can now define available shelter visit slots.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: DEFINE VISIT SLOTS
    @PostMapping("/adoption-requests/{id}/slots")
    public String createVisitSlots(@PathVariable Long id,
                                   @RequestParam("visitDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate visitDate,
                                   @RequestParam(value = "timeSlots", required = false) List<String> timeSlots,
                                   @RequestParam(value = "customTime", required = false) String customTime,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            List<String> allTimes = new java.util.ArrayList<>();
            if (timeSlots != null) {
                allTimes.addAll(timeSlots);
            }
            if (customTime != null && !customTime.isBlank()) {
                for (String t : customTime.split(",")) {
                    if (!t.isBlank()) allTimes.add(t.trim());
                }
            }
            if (allTimes.isEmpty()) {
                throw new IllegalArgumentException("Please select or enter at least one time slot.");
            }
            adoptionService.createVisitSlots(id, visitDate, allTimes, user);
            redirectAttributes.addFlashAttribute("successMessage", "Shelter visit slots added successfully! The applicant can now choose from these slots.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: REJECT APPLICATION
    @PostMapping("/adoption-requests/{id}/reject")
    public String rejectApplication(@PathVariable Long id,
                                    @RequestParam(required = false, defaultValue = "") String remarks,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.rejectByShelter(id, remarks, user);
            redirectAttributes.addFlashAttribute("successMessage", "Application rejected. Notification sent to applicant.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: CONFIRM VISIT
    @PostMapping("/adoption-requests/{id}/confirm-visit")
    public String confirmVisit(@PathVariable Long id,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.confirmVisit(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Shelter visit confirmed! Meeting details sent to applicant.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: RESCHEDULE VISIT
    @PostMapping("/adoption-requests/{id}/reschedule-visit")
    public String rescheduleVisit(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "") String remarks,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.rescheduleVisit(id, remarks, user);
            redirectAttributes.addFlashAttribute("successMessage", "Visit slot released for rescheduling. Applicant notified.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: REJECT VISIT
    @PostMapping("/adoption-requests/{id}/reject-visit")
    public String rejectVisit(@PathVariable Long id,
                              @RequestParam(required = false, defaultValue = "") String remarks,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.rejectVisit(id, remarks, user);
            redirectAttributes.addFlashAttribute("successMessage", "Visit request rejected. Applicant has been notified.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: MARK VISIT COMPLETED
    @PostMapping("/adoption-requests/{id}/complete-visit")
    public String completeVisit(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.markVisitCompleted(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Shelter visit marked as COMPLETED. Please submit your adoption recommendation below.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: SHELTER RECOMMENDATION
    @PostMapping("/adoption-requests/{id}/recommend")
    public String submitRecommendation(@PathVariable Long id,
                                       @RequestParam("recommended") boolean recommended,
                                       @RequestParam(required = false, defaultValue = "") String notes,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.submitRecommendation(id, recommended, notes, user);
            if (recommended) {
                redirectAttributes.addFlashAttribute("successMessage", "Recommendation submitted! Application escalated to Admin for Final Review.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Application closed following negative visit recommendation.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    // ACTION: PET HANDOVER COMPLETED
    @PostMapping("/adoption-requests/{id}/handover")
    public String completeHandover(@PathVariable Long id,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            adoptionService.completeHandover(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "🎉 Pet Handover Completed! The pet has been officially marked as ADOPTED!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/owner/adoption-requests/" + id;
    }

    @GetMapping("/care-requests")
    public String careRequests(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter != null) {
            List<CareBooking> requests = careBookingService.getShelterRequests(shelter);
            model.addAttribute("requests", requests);
            model.addAttribute("shelter", shelter);
            int availableToday = careBookingService.calculateAvailableCapacity(shelter, LocalDate.now(), LocalDate.now());
            model.addAttribute("availableCapacityToday", availableToday);
        } else {
            model.addAttribute("requests", Collections.emptyList());
        }
        return "owner/care-requests";
    }

    @GetMapping("/care-requests/{id}")
    public String careRequestDetails(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        CareBooking booking = careBookingService.getBookingById(id);
        if (booking == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Care booking request not found.");
            return "redirect:/owner/care-requests";
        }
        Shelter shelter = getShelterForOwner(user);
        if (shelter == null || booking.getShelter() == null || !booking.getShelter().getId().equals(shelter.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to view this care booking.");
            return "redirect:/owner/care-requests";
        }
        int availableForStay = careBookingService.calculateAvailableCapacity(shelter, booking.getStartDate(), booking.getEndDate());
        model.addAttribute("booking", booking);
        model.addAttribute("careRequest", booking);
        model.addAttribute("request", booking);
        model.addAttribute("shelter", shelter);
        model.addAttribute("loggedInUser", user);
        model.addAttribute("availableForStay", availableForStay);
        return "owner/care-request-details";
    }

    @PostMapping("/care-requests/{id}/accept")
    public String acceptCareRequest(@PathVariable Long id,
                                    HttpServletRequest request,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.acceptRequest(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Care booking request accepted! A slot has been reserved.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/care-requests/" + id)) {
            return "redirect:/owner/care-requests/" + id;
        }
        return "redirect:/owner/care-requests";
    }

    @PostMapping("/care-requests/{id}/reject")
    public String rejectCareRequest(@PathVariable Long id,
                                    @RequestParam(required = false, defaultValue = "") String reason,
                                    HttpServletRequest request,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.rejectRequest(id, reason, user);
            redirectAttributes.addFlashAttribute("successMessage", "Care booking request rejected. Notification sent to owner.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/care-requests/" + id)) {
            return "redirect:/owner/care-requests/" + id;
        }
        return "redirect:/owner/care-requests";
    }

    @PostMapping("/care-requests/{id}/check-in")
    public String checkInPet(@PathVariable Long id,
                             HttpServletRequest request,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.checkInPet(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Pet checked in successfully! Pet is now under Active Stays.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/care-requests/" + id)) {
            return "redirect:/owner/care-requests/" + id;
        }
        return "redirect:/owner/current-stays";
    }

    @PostMapping("/care-requests/{id}/check-out")
    public String checkOutPet(@PathVariable Long id,
                              HttpServletRequest request,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.checkOutPet(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Pet checked out successfully! Care stay is COMPLETED and capacity released. Pet remains with original owner.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/care-requests/" + id)) {
            return "redirect:/owner/care-requests/" + id;
        }
        return "redirect:/owner/current-stays";
    }

    @GetMapping("/current-stays")
    public String currentStays(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = getShelterForOwner(user);
        if (shelter != null) {
            List<CareBooking> stays = careBookingService.getCurrentStays(shelter);
            model.addAttribute("stays", stays);
            model.addAttribute("shelter", shelter);
            int availableToday = careBookingService.calculateAvailableCapacity(shelter, LocalDate.now(), LocalDate.now());
            model.addAttribute("availableCapacityToday", availableToday);
        } else {
            model.addAttribute("stays", Collections.emptyList());
        }
        return "owner/current-stays";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("notifications", notifications);
        notificationService.markAllAsRead(user);
        return "owner/notifications";
    }
}
