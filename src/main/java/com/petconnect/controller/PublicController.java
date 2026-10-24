package com.petconnect.controller;

import com.petconnect.dto.UserRegistrationDto;
import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import com.petconnect.service.NotificationService;
import com.petconnect.service.PetService;
import com.petconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;
import com.petconnect.service.PasswordResetService;
import com.petconnect.entity.PasswordResetToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    private final UserService userService;
    private final PetService petService;
    private final NotificationService notificationService;
    private final com.petconnect.service.FavoriteService favoriteService;
    private final PasswordResetService passwordResetService;

    @Autowired
    public PublicController(UserService userService, 
                            PetService petService,
                            NotificationService notificationService,
                            com.petconnect.service.FavoriteService favoriteService,
                            PasswordResetService passwordResetService) {
        this.userService = userService;
        this.petService = petService;
        this.notificationService = notificationService;
        this.favoriteService = favoriteService;
        this.passwordResetService = passwordResetService;
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

    @GetMapping("/")
    public String home(Model model) {
        List<Pet> availablePets = petService.getAvailablePets();
        List<Shelter> allShelters = petService.getAllShelters();

        model.addAttribute("totalPets", availablePets.size() + 240);
        model.addAttribute("totalShelters", allShelters.size() + 115);
        model.addAttribute("happyAdoptions", 1500);
        model.addAttribute("totalBookings", 300);

        List<Pet> featuredPets = availablePets.stream().limit(4).collect(Collectors.toList());
        model.addAttribute("featuredPets", featuredPets);

        return "public/home";
    }

    @GetMapping("/browse-pets")
    public String browsePets(@RequestParam(required = false) String search,
                             @RequestParam(required = false) String type,
                             @RequestParam(required = false) String breed,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) Integer age,
                             Model model,
                             Principal principal) {
        
        List<Pet> filteredPets = petService.filterPets(search, type, breed, gender, age);

        List<String> allBreeds = petService.getAvailableBreeds();
        List<String> dogBreeds = petService.getAvailableBreedsByType("DOG");
        List<String> catBreeds = petService.getAvailableBreedsByType("CAT");

        List<String> displayBreeds = allBreeds;
        if (type != null && !type.isBlank()) {
            String normType = type.trim().toLowerCase();
            if (normType.startsWith("dog")) {
                displayBreeds = dogBreeds;
            } else if (normType.startsWith("cat")) {
                displayBreeds = catBreeds;
            }
        }

        Set<Long> favoritePetIds = Collections.emptySet();
        if (principal != null) {
            User currentUser = userService.findByEmail(principal.getName());
            if (currentUser != null) {
                favoritePetIds = favoriteService.getFavoritePetIdsForUser(currentUser.getId());
            }
        }

        model.addAttribute("pets", filteredPets);
        model.addAttribute("favoritePetIds", favoritePetIds);
        model.addAttribute("search", search != null ? search.trim() : "");
        model.addAttribute("selectedType", type != null ? type.trim() : "");
        model.addAttribute("selectedBreed", breed != null ? breed.trim() : "");
        model.addAttribute("selectedGender", gender != null ? gender.trim() : "");
        model.addAttribute("selectedAge", age);
        model.addAttribute("displayBreeds", displayBreeds);
        model.addAttribute("allBreeds", allBreeds);
        model.addAttribute("dogBreeds", dogBreeds);
        model.addAttribute("catBreeds", catBreeds);
        
        return "public/browse-pets";
    }

    @GetMapping("/pet-details/{id}")
    public String petDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes, Principal principal) {
        Pet pet = petService.getPetById(id);
        if (pet == null || "REMOVED".equalsIgnoreCase(pet.getAdoptionStatus()) || "INACTIVE".equalsIgnoreCase(pet.getAdoptionStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet not found or is no longer available.");
            return "redirect:/browse-pets";
        }

        Shelter shelter = (pet.getShelter() != null) ? pet.getShelter() : null;

        boolean isFavorite = false;
        if (principal != null) {
            User currentUser = userService.findByEmail(principal.getName());
            if (currentUser != null) {
                isFavorite = favoriteService.isFavorite(currentUser.getId(), pet.getId());
            }
        }

        model.addAttribute("pet", pet);
        model.addAttribute("shelter", shelter);
        model.addAttribute("isFavorite", isFavorite);
        return "public/pet-details";
    }

    @GetMapping("/login")
    public String login() {
        return "public/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "public/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@RequestParam(name = "email", required = false) String email,
                                       HttpServletRequest request,
                                       Model model) {
        if (email != null && !email.isBlank()) {
            String cleanEmail = email.trim();
            String baseUrl = request.getScheme() + "://" + request.getServerName() +
                    (request.getServerPort() == 80 || request.getServerPort() == 443 ? "" : ":" + request.getServerPort()) +
                    request.getContextPath();
            try {
                passwordResetService.processForgotPassword(cleanEmail, baseUrl);
                model.addAttribute("infoMessage", "If an account exists for this email, a password reset link has been sent.");
            } catch (org.springframework.mail.MailException e) {
                log.error("Password reset email could not be sent to {}: {}", cleanEmail, e.getMessage(), e);
                model.addAttribute("errorMessage", "Unable to send the password reset email right now. Please try again later.");
            } catch (Exception e) {
                log.error("Failed to process password reset for {}: {}", cleanEmail, e.getMessage(), e);
                model.addAttribute("errorMessage", "Unable to send the password reset email right now. Please try again later.");
            }
        } else {
            model.addAttribute("errorMessage", "Please enter a valid email address.");
        }
        model.addAttribute("submittedEmail", email);
        return "public/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam(name = "token", required = false) String token, Model model) {
        if (token == null || token.isBlank()) {
            model.addAttribute("invalidToken", true);
            model.addAttribute("errorMessage", "This password reset link is invalid or has expired.");
            return "public/reset-password";
        }

        PasswordResetToken resetToken = passwordResetService.validateToken(token.trim());
        if (resetToken == null) {
            model.addAttribute("invalidToken", true);
            model.addAttribute("errorMessage", "This password reset link is invalid or has expired.");
            return "public/reset-password";
        }

        model.addAttribute("validToken", true);
        model.addAttribute("token", token.trim());
        return "public/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(@RequestParam("token") String token,
                                      @RequestParam("password") String password,
                                      @RequestParam("confirmPassword") String confirmPassword,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        if (token == null || token.isBlank()) {
            model.addAttribute("invalidToken", true);
            model.addAttribute("errorMessage", "This password reset link is invalid or has expired.");
            return "public/reset-password";
        }

        PasswordResetToken resetToken = passwordResetService.validateToken(token.trim());
        if (resetToken == null) {
            model.addAttribute("invalidToken", true);
            model.addAttribute("errorMessage", "This password reset link is invalid or has expired.");
            return "public/reset-password";
        }

        if (password == null || password.length() < 6) {
            model.addAttribute("validToken", true);
            model.addAttribute("token", token.trim());
            model.addAttribute("errorMessage", "Password must be at least 6 characters long.");
            return "public/reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("validToken", true);
            model.addAttribute("token", token.trim());
            model.addAttribute("errorMessage", "Passwords do not match.");
            return "public/reset-password";
        }

        boolean success = passwordResetService.resetPassword(token.trim(), password);
        if (success) {
            return "redirect:/login?resetSuccess=true";
        } else {
            model.addAttribute("invalidToken", true);
            model.addAttribute("errorMessage", "This password reset link is invalid or has expired.");
            return "public/reset-password";
        }
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        model.addAttribute("currentStep", 1);
        return "public/register";
    }

    @PostMapping("/register")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      Model model) {
        
        // 1. Password confirmation match check
        if (registrationDto.getPassword() != null && registrationDto.getConfirmPassword() != null) {
            if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
                result.rejectValue("confirmPassword", null, "Passwords do not match.");
            }
        }

        // 2. Email uniqueness check
        if (registrationDto.getEmail() != null && !registrationDto.getEmail().isBlank()) {
            User existing = userService.findByEmail(registrationDto.getEmail().trim());
            if (existing != null) {
                result.rejectValue("email", null, "There is already an account registered with this email address.");
            }
        }

        // 3. Step 2 validations if registering as SHELTER_OWNER
        boolean isOwner = "owner".equalsIgnoreCase(registrationDto.getRole()) ||
                          "ROLE_SHELTER_OWNER".equalsIgnoreCase(registrationDto.getRole()) ||
                          "shelter_owner".equalsIgnoreCase(registrationDto.getRole());

        if (isOwner) {
            if (registrationDto.getShelterName() == null || registrationDto.getShelterName().trim().isEmpty()) {
                result.rejectValue("shelterName", null, "Shelter Name is required.");
            }
            if (registrationDto.getArea() == null || registrationDto.getArea().trim().isEmpty()) {
                result.rejectValue("area", null, "Area / Place is required.");
            }
            if (registrationDto.getCity() == null || registrationDto.getCity().trim().isEmpty()) {
                result.rejectValue("city", null, "City is required.");
            }
            if (registrationDto.getState() == null || registrationDto.getState().trim().isEmpty()) {
                result.rejectValue("state", null, "State is required.");
            }
            if (registrationDto.getAddress() == null || registrationDto.getAddress().trim().isEmpty()) {
                result.rejectValue("address", null, "Full Physical Address is required.");
            }
            if (registrationDto.getLocation() == null || registrationDto.getLocation().trim().isEmpty()) {
                result.rejectValue("location", null, "General Location is required.");
            }
        }

        if (result.hasErrors()) {
            boolean hasStep2Error = result.hasFieldErrors("shelterName") || 
                                    result.hasFieldErrors("area") || 
                                    result.hasFieldErrors("city") || 
                                    result.hasFieldErrors("state") || 
                                    result.hasFieldErrors("address") || 
                                    result.hasFieldErrors("location");
            model.addAttribute("currentStep", (isOwner && hasStep2Error) ? 2 : 1);
            return "public/register";
        }

        userService.save(registrationDto);
        if (isOwner) {
            return "redirect:/login?shelterRegistered=true";
        }
        return "redirect:/login?success=true";
    }

    @GetMapping("/how-it-works")
    public String howItWorks() {
        return "public/how-it-works";
    }

    @GetMapping("/contact")
    public String contact() {
        return "public/contact";
    }

    @GetMapping("/notifications")
    public String notifications(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(principal.getName());
        if (user != null && user.getRoles() != null) {
            boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            if (isAdmin) return "redirect:/admin/notifications";
            boolean isOwner = user.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_SHELTER_OWNER"));
            if (isOwner) return "redirect:/owner/notifications";
        }
        return "redirect:/user/notifications";
    }

    @GetMapping("/access-denied")
    public String accessDenied(jakarta.servlet.http.HttpServletResponse response, Model model, Principal principal) {
        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
        return "error/403";
    }
}
