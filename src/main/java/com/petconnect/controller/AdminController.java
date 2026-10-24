package com.petconnect.controller;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionStatus;
import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import com.petconnect.entity.Notification;
import com.petconnect.entity.CareBooking;
import com.petconnect.entity.ShelterReview;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.repository.UserRepository;
import com.petconnect.repository.NotificationRepository;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.NotificationService;
import com.petconnect.service.PetService;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import com.petconnect.dto.AdminPendingAuditDto;

@Controller
@RequestMapping("/admin")
@org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final AdoptionService adoptionService;
    private final NotificationService notificationService;
    private final PetService petService;
    private final ShelterRepository shelterRepository;
    private final UserRepository userRepository;
    private final com.petconnect.service.ShelterReviewService shelterReviewService;
    private final com.petconnect.repository.PetRepository petRepository;
    private final com.petconnect.repository.AdoptionApplicationRepository adoptionApplicationRepository;
    private final com.petconnect.repository.CareBookingRepository careBookingRepository;
    private final com.petconnect.repository.ShelterReviewRepository shelterReviewRepository;
    private final com.petconnect.repository.ShelterVisitSlotRepository shelterVisitSlotRepository;
    private final NotificationRepository notificationRepository;
    private final com.petconnect.repository.ShelterDocumentRepository shelterDocumentRepository;
    private final com.petconnect.repository.ShelterVerificationAuditRepository shelterVerificationAuditRepository;

    @Autowired
    public AdminController(UserService userService,
                           AdoptionService adoptionService,
                           NotificationService notificationService,
                           PetService petService,
                           ShelterRepository shelterRepository,
                           UserRepository userRepository,
                           com.petconnect.service.ShelterReviewService shelterReviewService,
                           com.petconnect.repository.PetRepository petRepository,
                           com.petconnect.repository.AdoptionApplicationRepository adoptionApplicationRepository,
                           com.petconnect.repository.CareBookingRepository careBookingRepository,
                           com.petconnect.repository.ShelterReviewRepository shelterReviewRepository,
                           com.petconnect.repository.ShelterVisitSlotRepository shelterVisitSlotRepository,
                           NotificationRepository notificationRepository,
                           com.petconnect.repository.ShelterDocumentRepository shelterDocumentRepository,
                           com.petconnect.repository.ShelterVerificationAuditRepository shelterVerificationAuditRepository) {
        this.userService = userService;
        this.adoptionService = adoptionService;
        this.notificationService = notificationService;
        this.petService = petService;
        this.shelterRepository = shelterRepository;
        this.userRepository = userRepository;
        this.shelterReviewService = shelterReviewService;
        this.petRepository = petRepository;
        this.adoptionApplicationRepository = adoptionApplicationRepository;
        this.careBookingRepository = careBookingRepository;
        this.shelterReviewRepository = shelterReviewRepository;
        this.shelterVisitSlotRepository = shelterVisitSlotRepository;
        this.notificationRepository = notificationRepository;
        this.shelterDocumentRepository = shelterDocumentRepository;
        this.shelterVerificationAuditRepository = shelterVerificationAuditRepository;
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

    public List<AdminPendingAuditDto> getAllPendingAudits() {
        List<AdminPendingAuditDto> audits = new ArrayList<>();

        // 1. Pending Shelters
        List<Shelter> pendingShelters = shelterRepository.findAll().stream()
                .filter(s -> (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus()))
                        && !"INACTIVE".equalsIgnoreCase(s.getVerificationStatus())
                        && "PENDING".equalsIgnoreCase(s.getVerificationStatus()))
                .collect(Collectors.toList());
        for (Shelter s : pendingShelters) {
            String ownerName = s.getOwner() != null ? s.getOwner().getFullName() : "Registered User";
            audits.add(new AdminPendingAuditDto(
                    s.getId(),
                    "SHELTER",
                    "Shelter Verification",
                    "🏠",
                    s.getName(),
                    "Owner: " + ownerName,
                    s.getCreatedAt() != null ? s.getCreatedAt() : java.time.LocalDateTime.now(),
                    "PENDING",
                    "bg-warning text-dark",
                    "/admin/shelter-verification/" + s.getId()
            ));
        }

        // 2. Pending Pet Listings (Shelter pets)
        List<Pet> pendingPets = petRepository.findAllByOrderByIdDesc().stream()
                .filter(p -> p.getShelter() != null)
                .filter(p -> !"REMOVED".equalsIgnoreCase(p.getAdoptionStatus()) && !"INACTIVE".equalsIgnoreCase(p.getAdoptionStatus()))
                .filter(p -> !"INACTIVE".equalsIgnoreCase(p.getVerificationStatus()))
                .filter(p -> "PENDING".equalsIgnoreCase(p.getVerificationStatus()))
                .collect(Collectors.toList());
        for (Pet p : pendingPets) {
            String shelterName = p.getShelter() != null ? p.getShelter().getName() : p.getShelterName();
            audits.add(new AdminPendingAuditDto(
                    p.getId(),
                    "PET",
                    "Pet Verification",
                    "🐶",
                    p.getName(),
                    "Shelter: " + shelterName,
                    p.getCreatedAt() != null ? p.getCreatedAt() : java.time.LocalDateTime.now(),
                    "PENDING",
                    "bg-warning text-dark",
                    "/admin/pet-verification"
            ));
        }

        // 3. Pending Adoption Final Reviews
        List<AdoptionApplication> pendingReviews = adoptionApplicationRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(a -> a.getStatus() == AdoptionStatus.ADMIN_REVIEW || a.getStatus() == AdoptionStatus.SHELTER_RECOMMENDED)
                .collect(Collectors.toList());
        for (AdoptionApplication app : pendingReviews) {
            String petName = app.getPet() != null ? app.getPet().getName() : "Pet";
            String applicantName = app.getFullName() != null && !app.getFullName().isBlank() ? app.getFullName() :
                    (app.getUser() != null ? app.getUser().getFullName() : "Applicant");
            java.time.LocalDateTime appDate = app.getUpdatedAt() != null ? app.getUpdatedAt() :
                    (app.getCreatedAt() != null ? app.getCreatedAt() : java.time.LocalDateTime.now());
            audits.add(new AdminPendingAuditDto(
                    app.getId(),
                    "ADOPTION",
                    "Adoption Review",
                    "📄",
                    petName,
                    "Applicant: " + applicantName,
                    appDate,
                    "PENDING ADMIN REVIEW",
                    "bg-info text-dark",
                    "/admin/adoption-requests/" + app.getId()
            ));
        }

        // Sort chronologically descending (most recent pending audits first)
        Collections.sort(audits);
        return audits;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userRepository.count();
        List<Shelter> allShelters = petService.getAllShelters();
        List<Pet> allPets = petService.getAllPetsForAdmin();
        List<Pet> availablePets = petService.getAvailablePets();

        List<AdminPendingAuditDto> allPendingAudits = getAllPendingAudits();
        List<AdminPendingAuditDto> recentPendingAudits = allPendingAudits.stream()
                .limit(10)
                .collect(Collectors.toList());

        long pendingShelterCount = allPendingAudits.stream().filter(a -> "SHELTER".equals(a.getType())).count();
        long pendingPetCount = allPendingAudits.stream().filter(a -> "PET".equals(a.getType())).count();
        long pendingAdoptionCount = allPendingAudits.stream().filter(a -> "ADOPTION".equals(a.getType())).count();

        long totalOwners = allShelters.stream()
                .map(Shelter::getOwner)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalOwners", totalOwners > 0 ? totalOwners : 1);
        model.addAttribute("totalShelters", allShelters.size());
        model.addAttribute("totalPets", allPets.size());
        model.addAttribute("availablePets", availablePets.size());
        model.addAttribute("pendingVerifications", pendingPetCount + pendingShelterCount);
        model.addAttribute("pendingAdoptions", pendingAdoptionCount);
        model.addAttribute("activeBookings", 0);

        // Recent Pending Audits model attributes
        model.addAttribute("recentPendingAudits", recentPendingAudits);
        model.addAttribute("totalPendingAuditsCount", allPendingAudits.size());

        // Backward compatibility for templates expecting specific sub-lists
        model.addAttribute("pendingPetsList", allPendingAudits.stream().filter(a -> "PET".equals(a.getType())).collect(Collectors.toList()));
        model.addAttribute("pendingSheltersList", allPendingAudits.stream().filter(a -> "SHELTER".equals(a.getType())).collect(Collectors.toList()));
        model.addAttribute("pendingReviewsList", allPendingAudits.stream().filter(a -> "ADOPTION".equals(a.getType())).collect(Collectors.toList()));
        return "admin/dashboard";
    }

    @GetMapping({"/pending-audits", "/audits"})
    public String pendingAudits(Model model) {
        List<AdminPendingAuditDto> allPendingAudits = getAllPendingAudits();
        model.addAttribute("pendingAudits", allPendingAudits);
        model.addAttribute("totalCount", allPendingAudits.size());
        return "admin/pending-audits";
    }

    // ==========================================
    // USERS MANAGEMENT
    // ==========================================

    @GetMapping("/users")
    public String usersManagement(@RequestParam(value = "search", required = false) String search,
                                  @RequestParam(value = "role", required = false, defaultValue = "ALL") String role,
                                  @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                                  Principal principal,
                                  Model model) {
        List<User> allUsers = userRepository.findAll().stream()
                .sorted((u1, u2) -> {
                    if (u1.getCreatedAt() != null && u2.getCreatedAt() != null) {
                        return u2.getCreatedAt().compareTo(u1.getCreatedAt());
                    }
                    return Long.compare(u2.getId() != null ? u2.getId() : 0, u1.getId() != null ? u1.getId() : 0);
                })
                .collect(Collectors.toList());

        long totalCount = allUsers.size();
        long activeCount = allUsers.stream().filter(User::isActive).count();
        long inactiveCount = totalCount - activeCount;
        long shelterOwnerCount = allUsers.stream().filter(u -> "SHELTER_OWNER".equalsIgnoreCase(u.getPrimaryRoleName())).count();
        long normalUserCount = allUsers.stream().filter(u -> "USER".equalsIgnoreCase(u.getPrimaryRoleName())).count();
        long adminCount = allUsers.stream().filter(u -> "ADMIN".equalsIgnoreCase(u.getPrimaryRoleName())).count();

        List<User> filtered = allUsers.stream()
                .filter(u -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean nameMatch = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                    boolean emailMatch = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                    boolean phoneMatch = u.getPhone() != null && u.getPhone().toLowerCase().contains(q);
                    return nameMatch || emailMatch || phoneMatch;
                })
                .filter(u -> {
                    if ("ALL".equalsIgnoreCase(role) || role == null || role.isBlank()) return true;
                    return role.equalsIgnoreCase(u.getPrimaryRoleName());
                })
                .filter(u -> {
                    if ("ALL".equalsIgnoreCase(status) || status == null || status.isBlank()) return true;
                    if ("ACTIVE".equalsIgnoreCase(status)) return u.isActive();
                    if ("INACTIVE".equalsIgnoreCase(status)) return !u.isActive();
                    return true;
                })
                .collect(Collectors.toList());

        // Precompute set of user IDs with associated records
        Set<Long> userIdsWithRecords = new HashSet<>();
        shelterRepository.findAll().forEach(s -> { if (s.getOwner() != null && s.getOwner().getId() != null) userIdsWithRecords.add(s.getOwner().getId()); });
        petRepository.findAll().forEach(p -> { if (p.getUser() != null && p.getUser().getId() != null) userIdsWithRecords.add(p.getUser().getId()); });
        adoptionApplicationRepository.findAll().forEach(a -> { if (a.getUser() != null && a.getUser().getId() != null) userIdsWithRecords.add(a.getUser().getId()); });
        careBookingRepository.findAll().forEach(b -> { if (b.getUser() != null && b.getUser().getId() != null) userIdsWithRecords.add(b.getUser().getId()); });
        shelterReviewRepository.findAll().forEach(r -> { if (r.getUser() != null && r.getUser().getId() != null) userIdsWithRecords.add(r.getUser().getId()); });

        model.addAttribute("users", filtered);
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("selectedRole", role != null ? role.toUpperCase() : "ALL");
        model.addAttribute("selectedStatus", status != null ? status.toUpperCase() : "ALL");

        model.addAttribute("totalUsersCount", totalCount);
        model.addAttribute("activeUsersCount", activeCount);
        model.addAttribute("inactiveUsersCount", inactiveCount);
        model.addAttribute("shelterOwnerCount", shelterOwnerCount);
        model.addAttribute("normalUserCount", normalUserCount);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("currentAdminEmail", principal != null ? principal.getName() : "");
        model.addAttribute("userIdsWithRecords", userIdsWithRecords);

        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUserDetails(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User with ID #" + id + " not found.");
            return "redirect:/admin/users";
        }

        boolean isCurrentAdmin = principal != null && user.getEmail().equalsIgnoreCase(principal.getName());
        model.addAttribute("user", user);
        model.addAttribute("isCurrentAdmin", isCurrentAdmin);

        String role = user.getPrimaryRoleName();
        model.addAttribute("primaryRole", role);

        // Check if user has associated records
        boolean hasShelters = shelterRepository.findAll().stream().anyMatch(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()));
        boolean hasPets = petRepository.findAll().stream().anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()));
        boolean hasAdoptions = adoptionApplicationRepository.findAll().stream().anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(user.getId()));
        boolean hasBookings = careBookingRepository.findAll().stream().anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(user.getId()));
        boolean hasReviews = shelterReviewRepository.findAll().stream().anyMatch(r -> r.getUser() != null && r.getUser().getId().equals(user.getId()));
        boolean hasAssociatedRecords = hasShelters || hasPets || hasAdoptions || hasBookings || hasReviews;
        model.addAttribute("hasAssociatedRecords", hasAssociatedRecords);

        if ("SHELTER_OWNER".equalsIgnoreCase(role)) {
            Shelter shelter = shelterRepository.findAll().stream()
                    .filter(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("shelter", shelter);
            if (shelter != null) {
                List<Pet> shelterPets = petRepository.findByShelterOrderByIdDesc(shelter);
                List<AdoptionApplication> adoptionRequests = adoptionApplicationRepository.findByShelterIdOrderByCreatedAtDesc(shelter.getId());
                List<CareBooking> careBookings = careBookingRepository.findByShelterOrderByRequestedAtDesc(shelter);
                model.addAttribute("shelterPets", shelterPets);
                model.addAttribute("shelterPetsCount", shelterPets.size());
                model.addAttribute("adoptionRequests", adoptionRequests);
                model.addAttribute("adoptionRequestsCount", adoptionRequests.size());
                model.addAttribute("careBookings", careBookings);
                model.addAttribute("careBookingsCount", careBookings.size());
            } else {
                model.addAttribute("shelterPets", Collections.emptyList());
                model.addAttribute("shelterPetsCount", 0);
                model.addAttribute("adoptionRequests", Collections.emptyList());
                model.addAttribute("adoptionRequestsCount", 0);
                model.addAttribute("careBookings", Collections.emptyList());
                model.addAttribute("careBookingsCount", 0);
            }
        } else {
            // Normal USER activity
            List<Pet> userPets = petRepository.findByUserOrderByIdDesc(user);
            List<AdoptionApplication> userAdoptions = adoptionApplicationRepository.findByUserOrderByCreatedAtDesc(user);
            List<CareBooking> userBookings = careBookingRepository.findByUserOrderByRequestedAtDesc(user);
            List<ShelterReview> userReviews = shelterReviewRepository.findAll().stream()
                    .filter(r -> r.getUser() != null && r.getUser().getId().equals(user.getId()))
                    .collect(Collectors.toList());

            model.addAttribute("userPets", userPets);
            model.addAttribute("userPetsCount", userPets.size());
            model.addAttribute("userAdoptions", userAdoptions);
            model.addAttribute("userAdoptionsCount", userAdoptions.size());
            model.addAttribute("userBookings", userBookings);
            model.addAttribute("userBookingsCount", userBookings.size());
            model.addAttribute("userReviews", userReviews);
            model.addAttribute("userReviewsCount", userReviews.size());
        }

        return "admin/user-details";
    }

    @PostMapping("/users/{id}/activate")
    public String activateUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }
        user.setEnabled(true);
        user.setStatus("ACTIVE");
        userRepository.save(user);

        // If shelter owner, reactivate their shelter
        if ("SHELTER_OWNER".equalsIgnoreCase(user.getPrimaryRoleName())) {
            shelterRepository.findAll().stream()
                    .filter(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()))
                    .forEach(s -> {
                        s.setStatus("ACTIVE");
                        shelterRepository.save(s);
                    });
        }

        redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getFullName() + "' has been activated successfully!");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }
        if (principal != null && user.getEmail().equalsIgnoreCase(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot deactivate the currently logged-in Admin account.");
            return "redirect:/admin/users";
        }

        user.setEnabled(false);
        user.setStatus("INACTIVE");
        userRepository.save(user);

        // If shelter owner, deactivate their shelter so it does not appear as an active shelter
        if ("SHELTER_OWNER".equalsIgnoreCase(user.getPrimaryRoleName())) {
            shelterRepository.findAll().stream()
                    .filter(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()))
                    .forEach(s -> {
                        s.setStatus("INACTIVE");
                        shelterRepository.save(s);
                    });
        }

        redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getFullName() + "' has been deactivated. Login has been disabled while preserving all records.");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }
        if (principal != null && user.getEmail().equalsIgnoreCase(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You cannot delete the currently logged-in Admin account.");
            return "redirect:/admin/users";
        }

        // Check for dependent records
        boolean hasShelters = shelterRepository.findAll().stream().anyMatch(s -> s.getOwner() != null && s.getOwner().getId().equals(user.getId()));
        boolean hasPets = petRepository.findAll().stream().anyMatch(p -> p.getUser() != null && p.getUser().getId().equals(user.getId()));
        boolean hasAdoptions = adoptionApplicationRepository.findAll().stream().anyMatch(a -> a.getUser() != null && a.getUser().getId().equals(user.getId()));
        boolean hasBookings = careBookingRepository.findAll().stream().anyMatch(b -> b.getUser() != null && b.getUser().getId().equals(user.getId()));
        boolean hasReviews = shelterReviewRepository.findAll().stream().anyMatch(r -> r.getUser() != null && r.getUser().getId().equals(user.getId()));

        if (hasShelters || hasPets || hasAdoptions || hasBookings || hasReviews) {
            redirectAttributes.addFlashAttribute("errorMessage", "This user has associated records and cannot be permanently deleted. You can deactivate the account instead.");
            return "redirect:/admin/users";
        }

        try {
            // Delete notifications for user
            List<Notification> userNotifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
            if (userNotifications != null && !userNotifications.isEmpty()) {
                notificationRepository.deleteAll(userNotifications);
            }

            user.getRoles().clear();
            userRepository.save(user);
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("successMessage", "User '" + user.getFullName() + "' deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Could not delete user: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/shelter-verification")
    public String shelterVerification(@RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                                      @RequestParam(value = "search", required = false) String search,
                                      Model model) {
        List<Shelter> allShelters = shelterRepository.findAll();

        // Separate production shelters from demo/test shelters
        List<Shelter> productionShelters = allShelters.stream()
                .filter(s -> s.getVerificationMode() == null || "PRODUCTION".equalsIgnoreCase(s.getVerificationMode()))
                .collect(Collectors.toList());

        List<Shelter> demoShelters = allShelters.stream()
                .filter(s -> "DEMO".equalsIgnoreCase(s.getVerificationMode()))
                .collect(Collectors.toList());

        // Active shelters only for standard views
        boolean isInactiveQuery = "INACTIVE".equalsIgnoreCase(status);
        boolean isDemoQuery = "DEMO".equalsIgnoreCase(status);

        List<Shelter> candidatePool;
        if (isDemoQuery) {
            candidatePool = demoShelters;
        } else if (isInactiveQuery) {
            candidatePool = allShelters.stream()
                    .filter(s -> "INACTIVE".equalsIgnoreCase(s.getStatus()) || "INACTIVE".equalsIgnoreCase(s.getVerificationStatus()))
                    .collect(Collectors.toList());
        } else {
            candidatePool = productionShelters.stream()
                    .filter(s -> (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus()))
                            && !"INACTIVE".equalsIgnoreCase(s.getVerificationStatus()))
                    .collect(Collectors.toList());
        }

        // Strict isolation: Production statistics must NEVER include demo records
        long totalCount = productionShelters.stream()
                .filter(s -> (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus()))
                        && !"INACTIVE".equalsIgnoreCase(s.getVerificationStatus()))
                .count();
        long pendingCount = productionShelters.stream()
                .filter(s -> "PENDING".equalsIgnoreCase(s.getVerificationStatus())
                        && (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus())))
                .count();
        long manualReviewCount = productionShelters.stream()
                .filter(s -> "MANUAL_REVIEW".equalsIgnoreCase(s.getVerificationStatus())
                        && (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus())))
                .count();
        long autoRejectedCount = productionShelters.stream()
                .filter(s -> "AUTO_REJECTED".equalsIgnoreCase(s.getVerificationStatus())
                        && (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus())))
                .count();
        long approvedCount = productionShelters.stream()
                .filter(s -> "APPROVED".equalsIgnoreCase(s.getVerificationStatus())
                        && (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus())))
                .count();
        long rejectedCount = productionShelters.stream()
                .filter(s -> "REJECTED".equalsIgnoreCase(s.getVerificationStatus())
                        && (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus())))
                .count();

        long demoCount = demoShelters.size();

        List<Shelter> filtered = candidatePool.stream()
                .filter(s -> {
                    if (isDemoQuery || isInactiveQuery) return true;
                    if ("ALL".equalsIgnoreCase(status) || status == null || status.isBlank()) return true;
                    return status.equalsIgnoreCase(s.getVerificationStatus());
                })
                .filter(s -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean nameMatch = s.getName() != null && s.getName().toLowerCase().contains(q);
                    boolean locMatch = s.getLocation() != null && s.getLocation().toLowerCase().contains(q);
                    boolean cityMatch = s.getCity() != null && s.getCity().toLowerCase().contains(q);
                    boolean emailMatch = s.getEmail() != null && s.getEmail().toLowerCase().contains(q);
                    boolean ownerMatch = s.getOwner() != null && (
                            (s.getOwner().getFullName() != null && s.getOwner().getFullName().toLowerCase().contains(q)) ||
                            (s.getOwner().getEmail() != null && s.getOwner().getEmail().toLowerCase().contains(q))
                    );
                    return nameMatch || locMatch || cityMatch || emailMatch || ownerMatch;
                })
                .collect(Collectors.toList());

        model.addAttribute("shelters", filtered);
        model.addAttribute("currentStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("manualReviewCount", manualReviewCount);
        model.addAttribute("autoRejectedCount", autoRejectedCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("rejectedCount", rejectedCount);
        model.addAttribute("demoCount", demoCount);
        return "admin/shelter-verification";
    }

    @GetMapping({"/shelter-verification/{id}", "/shelters/{id}"})
    public String viewShelterDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Shelter shelter = shelterRepository.findById(id).orElse(null);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter with ID #" + id + " not found.");
            return "redirect:/admin/shelter-verification";
        }

        List<Pet> shelterPets = petService.getPetsByShelter(shelter);
        List<com.petconnect.entity.ShelterReview> shelterReviews = shelterReviewService.getReviewsByShelter(id);
        com.petconnect.entity.ShelterDocument latestDoc = shelterDocumentRepository.findFirstByShelterOrderByCreatedAtDesc(shelter).orElse(null);
        List<com.petconnect.entity.ShelterVerificationAudit> audits = shelterVerificationAuditRepository.findByShelterIdOrderByTimestampDesc(id);

        model.addAttribute("shelter", shelter);
        model.addAttribute("shelterPets", shelterPets);
        model.addAttribute("petsCount", shelterPets.size());
        model.addAttribute("shelterReviews", shelterReviews);
        model.addAttribute("reviewsCount", shelterReviews.size());
        model.addAttribute("latestDoc", latestDoc);
        model.addAttribute("verificationAudits", audits);
        return "admin/shelter-verification-details";
    }

    @GetMapping("/shelter-verification/{id}/ai-analysis")
    @ResponseBody
    public org.springframework.http.ResponseEntity<?> getShelterAiAnalysis(@PathVariable Long id) {
        Shelter shelter = shelterRepository.findById(id).orElse(null);
        if (shelter == null) {
            return org.springframework.http.ResponseEntity.notFound().build();
        }
        com.petconnect.entity.ShelterDocument latestDoc = shelterDocumentRepository.findFirstByShelterOrderByCreatedAtDesc(shelter).orElse(null);
        List<com.petconnect.entity.ShelterVerificationAudit> audits = shelterVerificationAuditRepository.findByShelterIdOrderByTimestampDesc(id);

        Map<String, Object> resp = new HashMap<>();
        resp.put("shelterId", shelter.getId());
        resp.put("shelterName", shelter.getName());
        resp.put("verificationStatus", shelter.getVerificationStatus());
        resp.put("confidenceScore", shelter.getVerificationConfidenceScore() != null ? shelter.getVerificationConfidenceScore() : (latestDoc != null ? latestDoc.getVerificationConfidenceScore() : null));
        resp.put("riskLevel", shelter.getRiskLevel() != null ? shelter.getRiskLevel() : (latestDoc != null ? latestDoc.getRiskLevel() : "UNKNOWN"));
        resp.put("aiRecommendation", shelter.getAiRecommendation());
        resp.put("hardFail", shelter.isHardFail());
        resp.put("autoRejected", shelter.isAutoRejected());
        resp.put("rejectionReasonCode", shelter.getRejectionReasonCode());
        resp.put("rejectionReason", shelter.getRejectionReason());
        resp.put("documentType", latestDoc != null ? latestDoc.getDocumentType() : "UNKNOWN");
        resp.put("nameMatch", latestDoc != null ? latestDoc.getNameMatch() : null);
        resp.put("addressMatch", latestDoc != null ? latestDoc.getAddressMatch() : null);
        resp.put("registrationNumberMatch", latestDoc != null ? latestDoc.getRegistrationNumberMatch() : null);
        resp.put("registrationNumberDetected", latestDoc != null ? latestDoc.getRegistrationNumberDetected() : null);
        resp.put("extractedRegistrationNumber", latestDoc != null ? latestDoc.getExtractedRegistrationNumber() : shelter.getRegistrationNumber());
        resp.put("dateValid", latestDoc != null ? latestDoc.getDateValid() : null);
        resp.put("issueDate", latestDoc != null ? latestDoc.getIssueDate() : null);
        resp.put("expiryDate", latestDoc != null ? latestDoc.getExpiryDate() : null);
        resp.put("qrDetected", latestDoc != null ? latestDoc.getQrDetected() : false);
        resp.put("officialVerification", latestDoc != null ? latestDoc.getOfficialVerificationStatus() : "NOT_AVAILABLE");
        resp.put("tamperingIndicators", latestDoc != null ? latestDoc.getTamperingIndicators() : false);
        resp.put("verifiedAt", shelter.getVerifiedAt() != null ? shelter.getVerifiedAt().toString() : (latestDoc != null && latestDoc.getVerifiedAt() != null ? latestDoc.getVerifiedAt().toString() : null));
        resp.put("verifiedBy", shelter.getVerifiedBy());
        resp.put("reasons", shelter.getVerificationReasons());
        resp.put("audits", audits);
        resp.put("verificationMode", shelter.getVerificationMode());
        resp.put("isDemo", shelter.isDemoMode());
        resp.put("demoNotice", shelter.isDemoMode() ? "DEMO MODE — FOR TESTING ONLY: Demo results do not prove government document authenticity." : null);

        return org.springframework.http.ResponseEntity.ok(resp);
    }

    @PostMapping("/shelter-verification/{id}/approve")
    public String approveShelter(@PathVariable Long id,
                                 Principal principal,
                                 @RequestParam(value = "redirect", required = false) String redirect,
                                 RedirectAttributes redirectAttributes) {
        Shelter shelter = shelterRepository.findById(id).orElse(null);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter not found.");
            return "redirect:/admin/shelter-verification";
        }

        // Safety Rule: Prohibit approval for DEMO / TEST records
        if ("DEMO".equalsIgnoreCase(shelter.getVerificationMode()) || shelter.isDemoMode()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action Blocked: This shelter is marked as DEMO / TEST MODE. Demo records are simulated test data and cannot enter the production approval workflow.");
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        // Prevent duplicate approval if already APPROVED
        if ("APPROVED".equalsIgnoreCase(shelter.getVerificationStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This shelter has already been approved.");
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        // Safety Rule: Prohibit approval if AUTO_REJECTED (Critical Hard Fail)
        if ("AUTO_REJECTED".equalsIgnoreCase(shelter.getVerificationStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Action Blocked: This shelter was AUTO-REJECTED by the AI Verification Engine due to critical authenticity failure. Admin cannot approve an auto-rejected shelter directly without a valid resubmitted certificate.");
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        // Allow approval when status is PENDING or MANUAL_REVIEW
        if (!"PENDING".equalsIgnoreCase(shelter.getVerificationStatus()) && !"MANUAL_REVIEW".equalsIgnoreCase(shelter.getVerificationStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only shelters with PENDING or MANUAL_REVIEW verification status can be approved. Current status: " + shelter.getVerificationStatus());
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        shelter.setVerificationStatus("APPROVED");
        shelter.setVerifiedAt(java.time.LocalDateTime.now());
        shelter.setVerifiedBy(principal != null ? principal.getName() : "admin@petconnect.com");
        shelter.setRejectionReason(null);
        shelterRepository.save(shelter);

        // Update latest document record if present
        com.petconnect.entity.ShelterDocument latestDoc = shelterDocumentRepository.findFirstByShelterOrderByCreatedAtDesc(shelter).orElse(null);
        if (latestDoc != null) {
            latestDoc.setAdminDecision("APPROVED");
            latestDoc.setAdminDecisionReason("Approved by Admin");
            latestDoc.setVerifiedBy(principal != null ? principal.getName() : "admin@petconnect.com");
            latestDoc.setVerifiedAt(java.time.LocalDateTime.now());
            shelterDocumentRepository.save(latestDoc);
        }

        // Log audit record
        com.petconnect.entity.ShelterVerificationAudit audit = new com.petconnect.entity.ShelterVerificationAudit(
                shelter.getId(),
                latestDoc != null ? latestDoc.getId() : null,
                shelter.getVerificationConfidenceScore(),
                shelter.getRiskLevel(),
                false,
                "ADMIN_APPROVED",
                null,
                "Approved by Platform Administrator",
                principal != null ? principal.getName() : "admin@petconnect.com"
        );
        shelterVerificationAuditRepository.save(audit);

        if (shelter.getOwner() != null) {
            notificationService.createNotification(
                    shelter.getOwner(),
                    "Shelter Approved!",
                    "Congratulations! Your shelter '" + shelter.getName() + "' has been verified and approved by the platform administrator. You can now add rescued pets and accept pet care bookings.",
                    "SHELTER"
            );
        }
        redirectAttributes.addFlashAttribute("successMessage", "Shelter '" + shelter.getName() + "' has been verified and approved successfully!");

        if ("details".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/shelter-verification/" + id;
        }
        return "redirect:/admin/shelter-verification";
    }

    @PostMapping("/shelter-verification/{id}/reject")
    public String rejectShelter(@PathVariable Long id,
                                @RequestParam(value = "rejectionReason", required = false) String rejectionReason,
                                @RequestParam(value = "redirect", required = false) String redirect,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Shelter shelter = shelterRepository.findById(id).orElse(null);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter not found.");
            return "redirect:/admin/shelter-verification";
        }

        // Prohibit rejection if already APPROVED
        if ("APPROVED".equalsIgnoreCase(shelter.getVerificationStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This shelter is already APPROVED and cannot be rejected.");
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        // Allow rejection when status is PENDING or MANUAL_REVIEW
        if (!"PENDING".equalsIgnoreCase(shelter.getVerificationStatus()) && !"MANUAL_REVIEW".equalsIgnoreCase(shelter.getVerificationStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Only shelters with PENDING or MANUAL_REVIEW verification status can be rejected. Current status: " + shelter.getVerificationStatus());
            if ("details".equalsIgnoreCase(redirect)) {
                return "redirect:/admin/shelter-verification/" + id;
            }
            return "redirect:/admin/shelter-verification";
        }

        String reason = (rejectionReason != null && !rejectionReason.isBlank())
                ? rejectionReason.trim()
                : "The documents or details submitted could not be verified by the platform.";

        shelter.setVerificationStatus("REJECTED");
        shelter.setRejectionReason(reason);
        shelter.setVerifiedAt(java.time.LocalDateTime.now());
        shelter.setVerifiedBy(principal != null ? principal.getName() : "admin@petconnect.com");
        shelterRepository.save(shelter);

        // Update latest document record if present
        com.petconnect.entity.ShelterDocument latestDoc = shelterDocumentRepository.findFirstByShelterOrderByCreatedAtDesc(shelter).orElse(null);
        if (latestDoc != null) {
            latestDoc.setAdminDecision("REJECTED");
            latestDoc.setAdminDecisionReason(reason);
            latestDoc.setVerifiedBy(principal != null ? principal.getName() : "admin@petconnect.com");
            latestDoc.setVerifiedAt(java.time.LocalDateTime.now());
            shelterDocumentRepository.save(latestDoc);
        }

        // Log audit record
        com.petconnect.entity.ShelterVerificationAudit audit = new com.petconnect.entity.ShelterVerificationAudit(
                shelter.getId(),
                latestDoc != null ? latestDoc.getId() : null,
                shelter.getVerificationConfidenceScore(),
                shelter.getRiskLevel(),
                false,
                "ADMIN_REJECTED",
                null,
                "Rejected by Admin: " + reason,
                principal != null ? principal.getName() : "admin@petconnect.com"
        );
        shelterVerificationAuditRepository.save(audit);

        if (shelter.getOwner() != null) {
            notificationService.createNotification(
                    shelter.getOwner(),
                    "Shelter Verification Declined",
                    "Your shelter '" + shelter.getName() + "' verification was rejected. Reason: " + reason + ". Please update your shelter details and documents under My Shelter to request re-verification.",
                    "SHELTER"
            );
        }
        redirectAttributes.addFlashAttribute("successMessage", "Shelter '" + shelter.getName() + "' has been rejected. Rejection reason recorded.");

        if ("details".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/shelter-verification/" + id;
        }
        return "redirect:/admin/shelter-verification";
    }

    @PostMapping({"/shelter-verification/{id}/delete", "/shelters/{id}/delete"})
    @org.springframework.transaction.annotation.Transactional
    public String deleteShelter(@PathVariable Long id,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        Shelter shelter = shelterRepository.findById(id).orElse(null);
        if (shelter == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter not found.");
            return "redirect:/admin/shelter-verification";
        }

        // Safety check: verify existing relationships in MySQL
        List<AdoptionApplication> adoptions = adoptionApplicationRepository.findByShelterOrderByCreatedAtDesc(shelter);
        List<com.petconnect.entity.CareBooking> careBookings = careBookingRepository.findByShelterOrderByRequestedAtDesc(shelter);
        List<Pet> pets = petRepository.findByShelter(shelter);
        List<com.petconnect.entity.ShelterReview> reviews = shelterReviewRepository.findByShelterOrderByCreatedAtDesc(shelter);
        List<com.petconnect.entity.ShelterVisitSlot> visitSlots = shelterVisitSlotRepository.findByShelter(shelter);

        boolean hasAdoptionHistory = !adoptions.isEmpty();
        boolean hasCareHistory = !careBookings.isEmpty();

        // If shelter has important existing history:
        // DO NOT permanently delete. Instead: ACTIVE -> INACTIVE
        if (hasAdoptionHistory || hasCareHistory) {
            shelter.setStatus("INACTIVE");
            shelter.setVerificationStatus("INACTIVE");
            shelter.setTemporaryCareEnabled(false);
            shelterRepository.save(shelter);

            // Deactivate any currently available pets for this shelter so they don't appear in public adoption catalog
            for (Pet p : pets) {
                if ("AVAILABLE".equalsIgnoreCase(p.getAdoptionStatus())) {
                    p.setAdoptionStatus("INACTIVE");
                    petRepository.save(p);
                }
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "This shelter has existing records and cannot be permanently deleted. It has been deactivated instead.");
            return "redirect:/admin/shelter-verification";
        }

        // Otherwise: shelter has NO adoption requests and NO care bookings -> Allow permanent DELETE
        try {
            // 1. Delete visit slots for this test shelter if any
            if (!visitSlots.isEmpty()) {
                shelterVisitSlotRepository.deleteAll(visitSlots);
            }

            // 2. Delete reviews for this test shelter if any
            if (!reviews.isEmpty()) {
                shelterReviewRepository.deleteAll(reviews);
            }

            // 3. Delete test pets (since they have no adoption/care records)
            if (!pets.isEmpty()) {
                for (Pet p : pets) {
                    List<com.petconnect.entity.ShelterVisitSlot> pSlots = shelterVisitSlotRepository.findAll().stream()
                            .filter(s -> s.getPet() != null && s.getPet().getId().equals(p.getId()))
                            .collect(Collectors.toList());
                    if (!pSlots.isEmpty()) {
                        shelterVisitSlotRepository.deleteAll(pSlots);
                    }

                    List<com.petconnect.entity.ShelterReview> pReviews = shelterReviewRepository.findAll().stream()
                            .filter(r -> r.getPet() != null && r.getPet().getId().equals(p.getId()))
                            .collect(Collectors.toList());
                    if (!pReviews.isEmpty()) {
                        shelterReviewRepository.deleteAll(pReviews);
                    }

                    petRepository.delete(p);
                }
            }

            // 4. Detach owner to preserve the owner's user account
            shelter.setOwner(null);
            shelterRepository.save(shelter);

            // 5. Permanently delete the shelter record from MySQL
            shelterRepository.delete(shelter);

            redirectAttributes.addFlashAttribute("successMessage", "Shelter deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting shelter: " + e.getMessage());
        }

        return "redirect:/admin/shelter-verification";
    }

    // ==========================================
    // PETS CATALOG & VERIFICATION (ADMIN)
    // ==========================================

    @GetMapping("/pets")
    public String petsCatalog(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "status", required = false, defaultValue = "ALL") String status,
                              @RequestParam(value = "gender", required = false, defaultValue = "ALL") String gender,
                              @RequestParam(value = "type", required = false, defaultValue = "ALL") String type,
                              @RequestParam(value = "shelterId", required = false) Long shelterId,
                              Model model) {
        List<Pet> allPets = petRepository.findAllByOrderByIdDesc().stream()
                .filter(p -> !"REMOVED".equalsIgnoreCase(p.getAdoptionStatus()))
                .collect(Collectors.toList());

        long totalPetsCount = allPets.size();
        long pendingPetsCount = allPets.stream().filter(p -> "PENDING".equalsIgnoreCase(p.getVerificationStatus())).count();
        long approvedPetsCount = allPets.stream().filter(p -> "APPROVED".equalsIgnoreCase(p.getVerificationStatus())).count();
        long rejectedPetsCount = allPets.stream().filter(p -> "REJECTED".equalsIgnoreCase(p.getVerificationStatus()) || "REJECTED".equalsIgnoreCase(p.getAdoptionStatus())).count();
        long availablePetsCount = allPets.stream().filter(p -> "AVAILABLE".equalsIgnoreCase(p.getAdoptionStatus()) && "APPROVED".equalsIgnoreCase(p.getVerificationStatus())).count();
        long adoptedPetsCount = allPets.stream().filter(p -> "ADOPTED".equalsIgnoreCase(p.getAdoptionStatus())).count();

        final String searchTrim = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;
        final String statusUpper = (status != null && !status.isBlank()) ? status.trim().toUpperCase() : "ALL";
        final String genderUpper = (gender != null && !gender.isBlank()) ? gender.trim().toUpperCase() : "ALL";
        final String typeUpper = (type != null && !type.isBlank()) ? type.trim().toUpperCase() : "ALL";

        List<Pet> filteredPets = allPets.stream()
                .filter(pet -> {
                    if (searchTrim == null) return true;
                    boolean nameMatch = pet.getName() != null && pet.getName().toLowerCase().contains(searchTrim);
                    boolean breedMatch = pet.getBreed() != null && pet.getBreed().toLowerCase().contains(searchTrim);
                    boolean typeMatch = pet.getType() != null && pet.getType().toLowerCase().contains(searchTrim);
                    boolean shelterMatch = pet.getShelter() != null && (
                            (pet.getShelter().getName() != null && pet.getShelter().getName().toLowerCase().contains(searchTrim)) ||
                            (pet.getShelter().getCity() != null && pet.getShelter().getCity().toLowerCase().contains(searchTrim)) ||
                            (pet.getShelter().getLocation() != null && pet.getShelter().getLocation().toLowerCase().contains(searchTrim))
                    );
                    boolean ownerMatch = pet.getShelter() != null && pet.getShelter().getOwner() != null && (
                            (pet.getShelter().getOwner().getFullName() != null && pet.getShelter().getOwner().getFullName().toLowerCase().contains(searchTrim)) ||
                            (pet.getShelter().getOwner().getEmail() != null && pet.getShelter().getOwner().getEmail().toLowerCase().contains(searchTrim))
                    );
                    boolean directOwnerMatch = pet.getOwnerName() != null && pet.getOwnerName().toLowerCase().contains(searchTrim);
                    return nameMatch || breedMatch || typeMatch || shelterMatch || ownerMatch || directOwnerMatch;
                })
                .filter(pet -> {
                    if ("ALL".equals(statusUpper)) return true;
                    if ("PENDING".equals(statusUpper)) {
                        return "PENDING".equalsIgnoreCase(pet.getVerificationStatus());
                    } else if ("APPROVED".equals(statusUpper)) {
                        return "APPROVED".equalsIgnoreCase(pet.getVerificationStatus());
                    } else if ("REJECTED".equals(statusUpper)) {
                        return "REJECTED".equalsIgnoreCase(pet.getVerificationStatus()) || "REJECTED".equalsIgnoreCase(pet.getAdoptionStatus());
                    } else if ("AVAILABLE".equals(statusUpper)) {
                        return "AVAILABLE".equalsIgnoreCase(pet.getAdoptionStatus()) && "APPROVED".equalsIgnoreCase(pet.getVerificationStatus());
                    } else if ("ADOPTED".equals(statusUpper)) {
                        return "ADOPTED".equalsIgnoreCase(pet.getAdoptionStatus());
                    }
                    return true;
                })
                .filter(pet -> {
                    if ("ALL".equals(genderUpper)) return true;
                    return pet.getGender() != null && pet.getGender().trim().equalsIgnoreCase(genderUpper);
                })
                .filter(pet -> {
                    if ("ALL".equals(typeUpper)) return true;
                    return pet.getType() != null && pet.getType().trim().equalsIgnoreCase(typeUpper);
                })
                .filter(pet -> {
                    if (shelterId == null || shelterId <= 0) return true;
                    return pet.getShelter() != null && pet.getShelter().getId().equals(shelterId);
                })
                .collect(Collectors.toList());

        List<Shelter> shelters = shelterRepository.findAll().stream()
                .filter(s -> s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("pets", filteredPets);
        model.addAttribute("totalPetsCount", totalPetsCount);
        model.addAttribute("pendingPetsCount", pendingPetsCount);
        model.addAttribute("approvedPetsCount", approvedPetsCount);
        model.addAttribute("rejectedPetsCount", rejectedPetsCount);
        model.addAttribute("availablePetsCount", availablePetsCount);
        model.addAttribute("adoptedPetsCount", adoptedPetsCount);
        model.addAttribute("shelters", shelters);

        model.addAttribute("searchQuery", search != null ? search : "");
        model.addAttribute("selectedStatus", statusUpper);
        model.addAttribute("selectedGender", genderUpper);
        model.addAttribute("selectedType", typeUpper);
        model.addAttribute("selectedShelterId", shelterId);

        return "admin/pets";
    }

    @GetMapping("/pets/{id}")
    public String viewPetDirect(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Pet pet = petRepository.findById(id).orElse(null);
        if (pet == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found with ID #" + id);
            return "redirect:/admin/pets";
        }
        return "redirect:/admin/pets?search=" + java.net.URLEncoder.encode(pet.getName(), java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/pets/{id}/approve")
    public String approvePetFromCatalog(@PathVariable Long id,
                                       @RequestParam(value = "redirect", required = false, defaultValue = "catalog") String redirect,
                                       RedirectAttributes redirectAttributes) {
        try {
            Pet pet = petService.verifyPet(id, "APPROVED");
            if (pet.getShelter() != null && pet.getShelter().getOwner() != null) {
                notificationService.createNotification(
                        pet.getShelter().getOwner(),
                        "Pet Approved!",
                        "Your rescued pet '" + pet.getName() + "' has been verified and is now live in the public catalog.",
                        "PET"
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Pet '" + pet.getName() + "' has been APPROVED and is now live!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if ("verification".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/pet-verification";
        }
        return "redirect:/admin/pets";
    }

    @PostMapping("/pets/{id}/reject")
    public String rejectPetFromCatalog(@PathVariable Long id,
                                      @RequestParam(value = "reason", required = false) String reason,
                                      @RequestParam(value = "redirect", required = false, defaultValue = "catalog") String redirect,
                                      RedirectAttributes redirectAttributes) {
        try {
            Pet pet = petService.verifyPet(id, "REJECTED");
            String reasonText = (reason != null && !reason.isBlank()) ? reason.trim() : "Documentation or listing details could not be verified.";
            if (pet.getShelter() != null && pet.getShelter().getOwner() != null) {
                notificationService.createNotification(
                        pet.getShelter().getOwner(),
                        "Pet Verification Update",
                        "Your pet listing for '" + pet.getName() + "' was not approved. Reason: " + reasonText,
                        "PET"
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Pet listing '" + pet.getName() + "' has been rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if ("verification".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/pet-verification";
        }
        return "redirect:/admin/pets";
    }

    @GetMapping("/pet-verification")
    public String petVerification(Model model) {
        model.addAttribute("pets", petService.getAllPetsForAdmin());
        return "admin/pet-verification";
    }

    @PostMapping("/pet-verification/{id}/approve")
    public String approvePet(@PathVariable Long id,
                             @RequestParam(value = "redirect", required = false, defaultValue = "verification") String redirect,
                             RedirectAttributes redirectAttributes) {
        try {
            Pet pet = petService.verifyPet(id, "APPROVED");
            if (pet.getShelter() != null && pet.getShelter().getOwner() != null) {
                notificationService.createNotification(
                        pet.getShelter().getOwner(),
                        "Pet Approved!",
                        "Your rescued pet '" + pet.getName() + "' has been verified and is now available in the public catalog.",
                        "PET"
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Pet '" + pet.getName() + "' has been APPROVED and is now live!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        if ("catalog".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/pets";
        }
        return "redirect:/admin/pet-verification";
    }

    @PostMapping("/pet-verification/{id}/reject")
    public String rejectPet(@PathVariable Long id,
                            @RequestParam(value = "reason", required = false) String reason,
                            @RequestParam(value = "redirect", required = false, defaultValue = "verification") String redirect,
                            RedirectAttributes redirectAttributes) {
        try {
            Pet pet = petService.verifyPet(id, "REJECTED");
            String reasonText = (reason != null && !reason.isBlank()) ? reason.trim() : "Documentation or listing details could not be verified.";
            if (pet.getShelter() != null && pet.getShelter().getOwner() != null) {
                notificationService.createNotification(
                        pet.getShelter().getOwner(),
                        "Pet Verification Update",
                        "Your pet listing for '" + pet.getName() + "' was not approved. Reason: " + reasonText,
                        "PET"
                );
            }
            redirectAttributes.addFlashAttribute("successMessage", "Pet listing rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        if ("catalog".equalsIgnoreCase(redirect)) {
            return "redirect:/admin/pets";
        }
        return "redirect:/admin/pet-verification";
    }

    // REAL ADOPTION REQUESTS FROM MYSQL
    @GetMapping("/adoption-requests")
    public String adoptionRequests(Model model) {
        List<AdoptionApplication> applications = adoptionService.getAdminApplications();
        model.addAttribute("applications", applications);
        return "admin/adoption-requests";
    }

    // VIEW DETAILED APPLICATION FOR ADMIN
    @GetMapping("/adoption-requests/{id}")
    public String viewApplicationDetails(@PathVariable Long id, Model model) {
        AdoptionApplication application = adoptionService.getApplicationById(id);
        model.addAttribute("application", application);
        model.addAttribute("adoptionApplication", application);
        model.addAttribute("app", application);
        model.addAttribute("pet", application.getPet());
        model.addAttribute("shelter", application.getShelter());
        return "admin/adoption-details";
    }

    // ACTION: ADMIN APPROVE
    @PostMapping("/adoption-requests/{id}/approve")
    public String approveAdoption(@PathVariable Long id,
                                  @RequestParam(required = false, defaultValue = "") String remarks,
                                  Principal principal,
                                  RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User admin = userService.findByEmail(principal.getName());
        try {
            adoptionService.adminReview(id, true, remarks, admin);
            redirectAttributes.addFlashAttribute("successMessage", "🎉 Adoption officially APPROVED! The shelter owner has been notified to proceed with pet handover.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/adoption-requests/" + id;
    }

    // ACTION: ADMIN REJECT
    @PostMapping("/adoption-requests/{id}/reject")
    public String rejectAdoption(@PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "") String remarks,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User admin = userService.findByEmail(principal.getName());
        try {
            adoptionService.adminReview(id, false, remarks, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Adoption application rejected.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/adoption-requests/" + id;
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        List<Notification> notifications = notificationService.getUserNotifications(user);
        model.addAttribute("notifications", notifications);
        notificationService.markAllAsRead(user);
        return "admin/notifications";
    }
}
