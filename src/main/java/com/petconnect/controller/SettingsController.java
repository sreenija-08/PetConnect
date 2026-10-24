package com.petconnect.controller;

import com.petconnect.entity.NotificationPreference;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import com.petconnect.repository.ShelterRepository;
import com.petconnect.service.NotificationService;
import com.petconnect.service.SettingsService;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class SettingsController {

    private final UserService userService;
    private final SettingsService settingsService;
    private final ShelterRepository shelterRepository;
    private final NotificationService notificationService;
    private final com.petconnect.service.ShelterDocumentAiService shelterDocumentAiService;

    @Autowired
    public SettingsController(UserService userService,
                              SettingsService settingsService,
                              ShelterRepository shelterRepository,
                              NotificationService notificationService,
                              com.petconnect.service.ShelterDocumentAiService shelterDocumentAiService) {
        this.userService = userService;
        this.settingsService = settingsService;
        this.shelterRepository = shelterRepository;
        this.notificationService = notificationService;
        this.shelterDocumentAiService = shelterDocumentAiService;
    }

    @ModelAttribute
    public void addCommonAttributes(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            model.addAttribute("loggedInUser", user);
            if (user != null) {
                model.addAttribute("unreadNotificationsCount", notificationService.getUnreadCount(user));
            }
        }
    }

    // Central /settings router that redirects based on role
    @GetMapping("/settings")
    public String routeSettings(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return "redirect:/admin/settings";
            } else if ("ROLE_SHELTER_OWNER".equals(authority.getAuthority())) {
                return "redirect:/shelter/settings";
            }
        }
        return "redirect:/user/settings";
    }

    // ==========================================
    // USER SETTINGS (/user/settings)
    // ==========================================

    @GetMapping("/user/settings")
    public String userSettings(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        NotificationPreference preferences = settingsService.getOrCreatePreferences(user);
        model.addAttribute("user", user);
        model.addAttribute("preferences", preferences);
        return "user/settings";
    }

    @PostMapping("/user/settings/profile")
    public String updateUserProfile(@RequestParam String fullName,
                                    @RequestParam(required = false) String phone,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.updateProfile(user, fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update profile. Please try again.");
        }
        return "redirect:/user/settings";
    }

    @PostMapping("/user/settings/password")
    public String updateUserPassword(@RequestParam String currentPassword,
                                     @RequestParam String newPassword,
                                     @RequestParam String confirmPassword,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.changePassword(user, currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Please try again.");
        }
        return "redirect:/user/settings";
    }

    @PostMapping("/user/settings/notifications")
    public String updateUserNotificationPreferences(
            @RequestParam(defaultValue = "false") boolean emailAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean emailVisitUpdates,
            @RequestParam(defaultValue = "false") boolean emailCareUpdates,
            @RequestParam(defaultValue = "true") boolean emailSecurityUpdates,
            @RequestParam(defaultValue = "false") boolean inappAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean inappCareUpdates,
            @RequestParam(defaultValue = "false") boolean inappShelterUpdates,
            @RequestParam(defaultValue = "false") boolean inappSystemUpdates,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            NotificationPreference pref = new NotificationPreference();
            pref.setEmailAdoptionUpdates(emailAdoptionUpdates);
            pref.setEmailVisitUpdates(emailVisitUpdates);
            pref.setEmailCareUpdates(emailCareUpdates);
            pref.setEmailSecurityUpdates(emailSecurityUpdates);
            pref.setInappAdoptionUpdates(inappAdoptionUpdates);
            pref.setInappCareUpdates(inappCareUpdates);
            pref.setInappShelterUpdates(inappShelterUpdates);
            pref.setInappSystemUpdates(inappSystemUpdates);

            settingsService.updatePreferences(user, pref);
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update notification preferences.");
        }
        return "redirect:/user/settings";
    }

    // ==========================================
    // SHELTER OWNER SETTINGS (/shelter/settings & /owner/settings)
    // ==========================================

    @GetMapping({"/shelter/settings", "/owner/settings"})
    public String shelterSettings(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        NotificationPreference preferences = settingsService.getOrCreatePreferences(user);
        Shelter shelter = getShelterForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("preferences", preferences);
        model.addAttribute("shelter", shelter);
        return "shelter/settings";
    }

    @PostMapping({"/shelter/settings/profile", "/owner/settings/profile"})
    public String updateShelterProfile(@RequestParam String fullName,
                                       @RequestParam(required = false) String phone,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.updateProfile(user, fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update profile. Please try again.");
        }
        return "redirect:/shelter/settings";
    }

    @PostMapping({"/shelter/settings/password", "/owner/settings/password"})
    public String updateShelterPassword(@RequestParam String currentPassword,
                                        @RequestParam String newPassword,
                                        @RequestParam String confirmPassword,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.changePassword(user, currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Please try again.");
        }
        return "redirect:/shelter/settings";
    }

    @PostMapping({"/shelter/settings/notifications", "/owner/settings/notifications"})
    public String updateShelterNotificationPreferences(
            @RequestParam(defaultValue = "false") boolean emailAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean emailVisitUpdates,
            @RequestParam(defaultValue = "false") boolean emailCareUpdates,
            @RequestParam(defaultValue = "true") boolean emailSecurityUpdates,
            @RequestParam(defaultValue = "false") boolean inappAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean inappCareUpdates,
            @RequestParam(defaultValue = "false") boolean inappShelterUpdates,
            @RequestParam(defaultValue = "false") boolean inappSystemUpdates,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            NotificationPreference pref = new NotificationPreference();
            pref.setEmailAdoptionUpdates(emailAdoptionUpdates);
            pref.setEmailVisitUpdates(emailVisitUpdates);
            pref.setEmailCareUpdates(emailCareUpdates);
            pref.setEmailSecurityUpdates(emailSecurityUpdates);
            pref.setInappAdoptionUpdates(inappAdoptionUpdates);
            pref.setInappCareUpdates(inappCareUpdates);
            pref.setInappShelterUpdates(inappShelterUpdates);
            pref.setInappSystemUpdates(inappSystemUpdates);

            settingsService.updatePreferences(user, pref);
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update notification preferences.");
        }
        return "redirect:/shelter/settings";
    }

    // ==========================================
    // ADMIN SETTINGS (/admin/settings)
    // ==========================================

    @GetMapping("/admin/settings")
    public String adminSettings(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        NotificationPreference preferences = settingsService.getOrCreatePreferences(user);
        model.addAttribute("user", user);
        model.addAttribute("preferences", preferences);
        model.addAttribute("autoRejectThreshold", shelterDocumentAiService.getAutoRejectThreshold());
        model.addAttribute("lowRiskThreshold", shelterDocumentAiService.getLowRiskThreshold());
        return "admin/settings";
    }

    @PostMapping("/admin/settings/verification-thresholds")
    public String updateVerificationThresholds(@RequestParam int autoRejectThreshold,
                                               @RequestParam int lowRiskThreshold,
                                               Principal principal,
                                               RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        try {
            shelterDocumentAiService.updateThresholds(autoRejectThreshold, lowRiskThreshold);
            redirectAttributes.addFlashAttribute("successMessage",
                    "AI Document Verification thresholds updated successfully! (Auto-Reject: " + autoRejectThreshold + ", Low-Risk: " + lowRiskThreshold + ")");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/profile")
    public String updateAdminProfile(@RequestParam String fullName,
                                     @RequestParam(required = false) String phone,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.updateProfile(user, fullName, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update profile. Please try again.");
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/password")
    public String updateAdminPassword(@RequestParam String currentPassword,
                                      @RequestParam String newPassword,
                                      @RequestParam String confirmPassword,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            settingsService.changePassword(user, currentPassword, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to change password. Please try again.");
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/admin/settings/notifications")
    public String updateAdminNotificationPreferences(
            @RequestParam(defaultValue = "false") boolean emailAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean emailVisitUpdates,
            @RequestParam(defaultValue = "false") boolean emailCareUpdates,
            @RequestParam(defaultValue = "true") boolean emailSecurityUpdates,
            @RequestParam(defaultValue = "false") boolean inappAdoptionUpdates,
            @RequestParam(defaultValue = "false") boolean inappCareUpdates,
            @RequestParam(defaultValue = "false") boolean inappShelterUpdates,
            @RequestParam(defaultValue = "false") boolean inappSystemUpdates,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            NotificationPreference pref = new NotificationPreference();
            pref.setEmailAdoptionUpdates(emailAdoptionUpdates);
            pref.setEmailVisitUpdates(emailVisitUpdates);
            pref.setEmailCareUpdates(emailCareUpdates);
            pref.setEmailSecurityUpdates(emailSecurityUpdates);
            pref.setInappAdoptionUpdates(inappAdoptionUpdates);
            pref.setInappCareUpdates(inappCareUpdates);
            pref.setInappShelterUpdates(inappShelterUpdates);
            pref.setInappSystemUpdates(inappSystemUpdates);

            settingsService.updatePreferences(user, pref);
            redirectAttributes.addFlashAttribute("successMessage", "Notification preferences updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to update notification preferences.");
        }
        return "redirect:/admin/settings";
    }

    private Shelter getShelterForUser(User user) {
        if (user == null) return null;
        List<Shelter> shelters = shelterRepository.findByOwner(user);
        if (!shelters.isEmpty()) return shelters.get(0);
        if ("saranya@gmail.com".equalsIgnoreCase(user.getEmail())) {
            return shelterRepository.findByName("Happy Paws Shelter").orElse(null);
        }
        if ("owner@petconnect.com".equalsIgnoreCase(user.getEmail())) {
            return shelterRepository.findByName("Safe Tails Shelter").orElse(null);
        }
        return null;
    }
}
