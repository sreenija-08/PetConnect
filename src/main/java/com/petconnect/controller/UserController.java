package com.petconnect.controller;

import com.petconnect.entity.*;
import com.petconnect.repository.CareBookingRepository;
import com.petconnect.repository.PetRepository;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.NotificationService;
import com.petconnect.service.PetService;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/user")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;
    private final AdoptionService adoptionService;
    private final NotificationService notificationService;
    private final PetService petService;
    private final PetRepository petRepository;
    private final ShelterRepository shelterRepository;
    private final com.petconnect.service.CareBookingService careBookingService;
    private final com.petconnect.service.ShelterReviewService shelterReviewService;
    private final CareBookingRepository careBookingRepository;
    private final com.petconnect.service.FavoriteService favoriteService;
    private final com.petconnect.service.AdoptionMeetingService adoptionMeetingService;

    @Autowired
    public UserController(UserService userService,
                          AdoptionService adoptionService,
                          NotificationService notificationService,
                          PetService petService,
                          PetRepository petRepository,
                          ShelterRepository shelterRepository,
                          com.petconnect.service.CareBookingService careBookingService,
                          com.petconnect.service.ShelterReviewService shelterReviewService,
                          CareBookingRepository careBookingRepository,
                          com.petconnect.service.FavoriteService favoriteService,
                          com.petconnect.service.AdoptionMeetingService adoptionMeetingService) {
        this.userService = userService;
        this.adoptionService = adoptionService;
        this.notificationService = notificationService;
        this.petService = petService;
        this.petRepository = petRepository;
        this.shelterRepository = shelterRepository;
        this.careBookingService = careBookingService;
        this.shelterReviewService = shelterReviewService;
        this.careBookingRepository = careBookingRepository;
        this.favoriteService = favoriteService;
        this.adoptionMeetingService = adoptionMeetingService;
    }

    private List<Pet> getActiveUserPets(User user) {
        if (user == null) return Collections.emptyList();
        return petRepository.findByUserOrderByIdDesc(user)
                .stream()
                .filter(p -> !"REMOVED".equalsIgnoreCase(p.getAdoptionStatus()) 
                          && !"INACTIVE".equalsIgnoreCase(p.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    private String saveUploadedPetPhoto(MultipartFile file, Long userId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > 15 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 15MB limit.");
        }
        String origFilename = file.getOriginalFilename();
        if (origFilename == null || !origFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file: missing file extension.");
        }
        String ext = origFilename.substring(origFilename.lastIndexOf(".")).toLowerCase();
        if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".webp")) {
            throw new IllegalArgumentException("Unsupported file format. Please upload JPG, JPEG, PNG, or WEBP.");
        }
        String contentType = file.getContentType();
        if (contentType != null && !contentType.isBlank()) {
            String lowerCt = contentType.toLowerCase();
            if (!lowerCt.startsWith("image/") && !lowerCt.equals("application/octet-stream")) {
                throw new IllegalArgumentException("Invalid file type: must be an image.");
            }
        }
        try {
            Path uploadDir = Paths.get("uploads", "pets");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            String filename = "pet-user-" + userId + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12) + ext;
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

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            if (user != null) {
                List<AdoptionApplication> applications = adoptionService.getUserApplications(user);
                List<Pet> userPets = getActiveUserPets(user);
                List<CareBooking> careBookings = careBookingService.getUserBookings(user);
                List<Notification> notifications = notificationService.getUserNotifications(user);
                List<Pet> availablePets = petService.getAvailablePets();

                model.addAttribute("adoptionsCount", applications.size());
                model.addAttribute("bookingsCount", careBookings.size());
                model.addAttribute("favoritesCount", favoriteService.getFavoriteCountForUser(user.getId()));
                model.addAttribute("notificationsCount", notificationService.getUnreadCount(user));
                model.addAttribute("recentNotifications", notifications.stream().limit(5).collect(Collectors.toList()));
                model.addAttribute("recentApplications", applications.stream().limit(3).collect(Collectors.toList()));
                return "user/dashboard";
            }
        }
        model.addAttribute("adoptionsCount", 0);
        model.addAttribute("bookingsCount", 0);
        model.addAttribute("favoritesCount", 0);
        model.addAttribute("notificationsCount", 0);
        model.addAttribute("recentNotifications", Collections.emptyList());
        model.addAttribute("recentApplications", Collections.emptyList());
        return "user/dashboard";
    }

    @GetMapping("/my-pets")
    public String myPets(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<Pet> userPets = getActiveUserPets(user);
        model.addAttribute("pets", userPets);
        return "user/my-pets";
    }

    @GetMapping("/add-pet")
    public String addPetGet() {
        return "redirect:/user/my-pets";
    }

    @PostMapping("/add-pet")
    public String addPetForCare(@RequestParam String name,
                                @RequestParam String type,
                                @RequestParam String breed,
                                @RequestParam String gender,
                                @RequestParam Integer age,
                                @RequestParam(required = false) String size,
                                @RequestParam(required = false) String healthInfo,
                                @RequestParam(required = false) String vaccinationStatus,
                                @RequestParam(required = false) String specialCareInstructions,
                                @RequestParam(required = false, defaultValue = "") String description,
                                @RequestParam(required = false) String imagePath,
                                @RequestParam(value = "petPhoto", required = false) MultipartFile petPhoto,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type != null ? type.toUpperCase() : "DOG");
        pet.setBreed(breed);
        pet.setGender(gender);
        pet.setAge(age);
        pet.setSize(size != null ? size : "Medium");
        pet.setHealthInfo(healthInfo != null && !healthInfo.isBlank() ? healthInfo : "Healthy & Active");
        pet.setVaccinationStatus(vaccinationStatus != null && !vaccinationStatus.isBlank() ? vaccinationStatus : "Vaccinated");
        pet.setSpecialCareInstructions(specialCareInstructions);
        pet.setDescription(description != null && !description.isBlank() ? description : specialCareInstructions);
        pet.setUser(user);
        pet.setOwnerName(user.getFullName() != null ? user.getFullName() : user.getEmail());
        pet.setAdoptionStatus("OWNED");
        pet.setVerificationStatus("APPROVED");

        // Handle Photo Upload
        if (petPhoto != null && !petPhoto.isEmpty()) {
            try {
                String uploadedUrl = saveUploadedPetPhoto(petPhoto, user.getId());
                pet.setPhotoUrl(uploadedUrl);
                pet.setImagePath(uploadedUrl);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Photo upload failed: " + e.getMessage());
                return "redirect:/user/my-pets";
            }
        } else if (imagePath != null && !imagePath.isBlank()) {
            pet.setImagePath(imagePath);
            pet.setPhotoUrl(imagePath);
        } else {
            String defaultImg = "DOG".equalsIgnoreCase(type) ? 
                "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800" :
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=800";
            pet.setImagePath(defaultImg);
        }
        petRepository.save(pet);

        redirectAttributes.addFlashAttribute("successMessage", "Your pet " + name + " has been registered successfully!");
        return "redirect:/user/my-pets";
    }

    @PostMapping("/update-pet-photo/{id}")
    public String updatePetPhoto(@PathVariable Long id,
                                 @RequestParam("petPhoto") MultipartFile petPhoto,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
            return "redirect:/user/my-pets";
        }

        // Security check: only the authenticated owner can update photo
        if (pet.getUser() == null || !pet.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to modify this pet.");
            return "redirect:/user/my-pets";
        }

        if (petPhoto == null || petPhoto.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select a photo to upload.");
            return "redirect:/user/my-pets";
        }

        try {
            // Delete old local photo file if it was previously uploaded
            deleteLocalPetPhoto(pet.getPhotoUrl());

            String newPhotoUrl = saveUploadedPetPhoto(petPhoto, user.getId());
            pet.setPhotoUrl(newPhotoUrl);
            pet.setImagePath(newPhotoUrl);
            petRepository.save(pet);
            redirectAttributes.addFlashAttribute("successMessage", "Photo for '" + pet.getName() + "' updated successfully!");
        } catch (IllegalArgumentException | SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload photo: " + e.getMessage());
        }

        return "redirect:/user/my-pets";
    }

    @PostMapping("/delete-pet/{id}")
    public String deleteUserPet(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found.");
            return "redirect:/user/my-pets";
        }

        // Security check: only the authenticated owner can delete their own pet
        if (pet.getUser() == null || !pet.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You do not have permission to delete this pet.");
            return "redirect:/user/my-pets";
        }

        // Prevent users from deleting shelter pets
        if (pet.getShelter() != null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter pets cannot be deleted from user accounts.");
            return "redirect:/user/my-pets";
        }

        // Check if pet has any care bookings
        List<CareBooking> bookings = careBookingRepository.findByPet(pet);
        if (bookings != null && !bookings.isEmpty()) {
            // Preserve booking history: safely deactivate pet
            pet.setAdoptionStatus("REMOVED");
            pet.setVerificationStatus("INACTIVE");
            petRepository.save(pet);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pet '" + pet.getName() + "' has been removed from your registered pets.");
        } else {
            // Delete local photo file if present
            deleteLocalPetPhoto(pet.getPhotoUrl());
            // No history: permanently delete
            petRepository.delete(pet);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Pet '" + pet.getName() + "' deleted successfully.");
        }

        return "redirect:/user/my-pets";
    }

    @GetMapping("/find-pet-care")
    public String findPetCare(@RequestParam(value = "location", required = false) String location,
                              @RequestParam(value = "startDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
                              @RequestParam(value = "endDate", required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
                              @RequestParam(value = "petType", required = false, defaultValue = "DOG") String petType,
                              Model model) {
        java.time.LocalDate start = startDate != null ? startDate : java.time.LocalDate.now().plusDays(2);
        java.time.LocalDate end = endDate != null ? endDate : start.plusDays(5);
        if (!end.isAfter(start)) {
            end = start.plusDays(1);
        }

        List<com.petconnect.dto.ShelterCareSearchResultDto> results = careBookingService.searchShelters(location, start, end, petType);
        model.addAttribute("shelterResults", results);
        model.addAttribute("location", location != null ? location : "");
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("petType", petType != null ? petType.toUpperCase() : "DOG");
        return "user/find-pet-care";
    }

    @GetMapping("/care-request")
    public String showCareRequestForm(@RequestParam Long shelterId,
                                      @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
                                      @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate,
                                      @RequestParam(required = false) String petType,
                                      Model model,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        Shelter shelter = shelterRepository.findById(shelterId).orElse(null);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter not found.");
            return "redirect:/user/find-pet-care";
        }

        List<Pet> userPets = getActiveUserPets(user);

        java.time.LocalDate start = startDate != null ? startDate : java.time.LocalDate.now().plusDays(2);
        java.time.LocalDate end = endDate != null ? endDate : start.plusDays(5);
        if (!end.isAfter(start)) {
            end = start.plusDays(1);
        }
        int days = (int) Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(start, end));
        double estTotal = days * shelter.getBoardingFee();

        model.addAttribute("shelter", shelter);
        model.addAttribute("userPets", userPets);
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("numberOfDays", days);
        model.addAttribute("estimatedTotal", estTotal);
        model.addAttribute("petType", petType);

        com.petconnect.dto.CareBookingRequestDto requestDto = new com.petconnect.dto.CareBookingRequestDto();
        requestDto.setShelterId(shelterId);
        requestDto.setStartDate(start);
        requestDto.setEndDate(end);
        if (user.getPhone() != null) {
            requestDto.setEmergencyContact(user.getPhone());
        }
        model.addAttribute("bookingRequest", requestDto);

        return "user/care-request";
    }

    @PostMapping("/care-request")
    public String submitCareRequest(@ModelAttribute com.petconnect.dto.CareBookingRequestDto dto,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.submitCareRequest(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Care request submitted successfully! The shelter owner will review your request shortly.");
            return "redirect:/user/care-bookings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/care-request?shelterId=" + dto.getShelterId() + 
                    (dto.getStartDate() != null ? "&startDate=" + dto.getStartDate() : "") +
                    (dto.getEndDate() != null ? "&endDate=" + dto.getEndDate() : "");
        }
    }

    @GetMapping("/care-bookings")
    public String careBookings(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<CareBooking> bookings = careBookingService.getUserBookings(user);
        model.addAttribute("bookings", bookings);
        return "user/care-bookings";
    }

    @PostMapping("/care-bookings/{id}/cancel")
    public String cancelCareBooking(@PathVariable Long id,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            careBookingService.cancelBooking(id, user);
            redirectAttributes.addFlashAttribute("successMessage", "Your pet care booking has been cancelled.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/care-bookings";
    }

    // REAL DATABASE BACKED USER ADOPTION REQUESTS
    @GetMapping("/adoption-requests")
    public String adoptionRequests(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<AdoptionApplication> applications = adoptionService.getUserApplications(user);
        List<com.petconnect.entity.ShelterReview> userReviews = shelterReviewService.getReviewsByUser(user);
        java.util.Set<Long> reviewedAppIds = userReviews.stream()
                .map(r -> r.getAdoptionApplication().getId())
                .collect(Collectors.toSet());
        java.util.Map<Long, com.petconnect.entity.ShelterReview> reviewsByAppId = userReviews.stream()
                .collect(Collectors.toMap(r -> r.getAdoptionApplication().getId(), r -> r, (r1, r2) -> r1));

        java.util.Map<Long, com.petconnect.entity.AdoptionMeeting> activeMeetingsByAppId = new java.util.HashMap<>();
        for (AdoptionApplication app : applications) {
            adoptionMeetingService.getActiveMeetingForApplication(app.getId())
                    .ifPresent(meeting -> activeMeetingsByAppId.put(app.getId(), meeting));
        }

        model.addAttribute("applications", applications);
        model.addAttribute("reviewedAppIds", reviewedAppIds);
        model.addAttribute("reviewsByAppId", reviewsByAppId);
        model.addAttribute("activeMeetingsByAppId", activeMeetingsByAppId);
        return "user/adoption-requests";
    }

    // REAL APPLICATION DETAIL & TIMELINE
    @GetMapping("/adoption-requests/{id}")
    public String viewApplicationDetails(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        AdoptionApplication application = adoptionService.getApplicationById(id);

        if (application == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adoption request not found.");
            return "redirect:/user/adoption-requests";
        }

        // Security check: only the applicant or authorized users can view
        if (application.getUser() == null || !application.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Access Denied: You do not have permission to view another user's adoption request.");
            return "redirect:/user/adoption-requests";
        }

        java.util.Optional<com.petconnect.entity.ShelterReview> existingReview = shelterReviewService.getReviewForApplication(id);
        model.addAttribute("existingReview", existingReview.orElse(null));
        model.addAttribute("hasReviewed", existingReview.isPresent());
        model.addAttribute("canReview", application.getStatus() == com.petconnect.entity.AdoptionStatus.COMPLETED && !existingReview.isPresent());

        model.addAttribute("application", application);
        model.addAttribute("adoptionApplication", application);
        model.addAttribute("app", application);
        model.addAttribute("pet", application.getPet());
        model.addAttribute("shelter", application.getShelter());
        model.addAttribute("availableSlots", adoptionService.getAvailableSlotsForApplication(id));

        // Video Consultation Meeting Details
        java.util.Optional<com.petconnect.entity.AdoptionMeeting> activeMeeting = adoptionMeetingService.getActiveMeetingForApplication(id);
        model.addAttribute("activeMeeting", activeMeeting.orElse(null));
        List<com.petconnect.entity.AdoptionMeeting> meetingHistory = adoptionMeetingService.getMeetingsForApplication(id);
        model.addAttribute("meetingHistory", meetingHistory);

        return "user/adoption-details";
    }

    @GetMapping("/favorites")
    public String favorites(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";
        List<Pet> favoritePets = favoriteService.getFavoritePetsForUser(user.getId());
        model.addAttribute("pets", favoritePets);
        return "user/favorites";
    }

    @PostMapping("/favorites/toggle/{petId}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> toggleFavoriteAjax(@PathVariable Long petId, Principal principal) {
        if (principal == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Unauthorized. Please login."));
        }
        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return org.springframework.http.ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "User session invalid."));
        }
        boolean isFavorite = favoriteService.toggleFavorite(user.getId(), petId);
        long count = favoriteService.getFavoriteCountForUser(user.getId());
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("isFavorite", isFavorite);
        resp.put("count", count);
        return org.springframework.http.ResponseEntity.ok(resp);
    }

    @PostMapping("/favorites/remove/{petId}")
    public String removeFavorite(@PathVariable Long petId, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user != null) {
            favoriteService.removeFavorite(user.getId(), petId);
            redirectAttributes.addFlashAttribute("successMessage", "Removed pet from favorites.");
        }
        return "redirect:/user/favorites";
    }

    // REAL DATABASE BACKED NOTIFICATIONS
    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("notifications", notifications);
        // Mark all as read when opening notifications page
        notificationService.markAllAsRead(user);
        return "user/notifications";
    }

    @GetMapping("/profile")
    public String profile() {
        return "user/profile";
    }
}
