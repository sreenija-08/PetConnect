package com.petconnect.controller;

import com.petconnect.dto.PetMatchPreferenceDto;
import com.petconnect.dto.PetMatchResultDto;
import com.petconnect.entity.User;
import com.petconnect.entity.UserPetPreference;
import com.petconnect.service.NotificationService;
import com.petconnect.service.PetMatchService;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user/petmatch")
@PreAuthorize("hasRole('USER')")
public class PetMatchController {

    private final PetMatchService petMatchService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public PetMatchController(PetMatchService petMatchService,
                              UserService userService,
                              NotificationService notificationService) {
        this.petMatchService = petMatchService;
        this.userService = userService;
        this.notificationService = notificationService;
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

    /**
     * Preference questionnaire page.
     * Pre-fills previous user preferences if saved, or user's registered city.
     */
    @GetMapping
    public String showPreferencePage(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        Optional<UserPetPreference> savedPref = petMatchService.getUserPreferences(user);
        PetMatchPreferenceDto preferenceDto;

        if (savedPref.isPresent()) {
            preferenceDto = PetMatchPreferenceDto.fromEntity(savedPref.get());
            model.addAttribute("hasSavedPreferences", true);
        } else {
            preferenceDto = new PetMatchPreferenceDto();
            if (user.getCity() != null && !user.getCity().isBlank()) {
                preferenceDto.setPreferredLocation(user.getCity());
            }
            model.addAttribute("hasSavedPreferences", false);
        }

        model.addAttribute("preferences", preferenceDto);
        return "user/petmatch";
    }

    /**
     * Handles submission of lifestyle preferences, executes backend recommendation engine,
     * optionally saves preferences, and displays ranked matching results.
     */
    @PostMapping
    public String calculateMatches(@ModelAttribute("preferences") PetMatchPreferenceDto preferences,
                                   Model model,
                                   Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        // Save preferences if checkbox is ticked
        if (Boolean.TRUE.equals(preferences.getSavePreferences())) {
            petMatchService.saveUserPreferences(user, preferences);
        }

        // Run Recommendation Engine
        List<PetMatchResultDto> recommendations = petMatchService.getRecommendations(user, preferences);

        model.addAttribute("preferences", preferences);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("totalMatches", recommendations.size());

        // Count high matches (>= 75%)
        long highMatchCount = recommendations.stream().filter(r -> r.getCompatibilityScore() >= 75).count();
        model.addAttribute("highMatchCount", highMatchCount);

        return "user/petmatch-results";
    }

    /**
     * Direct navigation to latest results based on saved user preferences.
     */
    @GetMapping("/results")
    public String viewSavedResults(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        if (user == null) return "redirect:/login";

        Optional<UserPetPreference> savedPref = petMatchService.getUserPreferences(user);
        if (savedPref.isEmpty()) {
            redirectAttributes.addFlashAttribute("infoMessage", "Please tell us your lifestyle preferences first to view your pet recommendations.");
            return "redirect:/user/petmatch";
        }

        PetMatchPreferenceDto dto = PetMatchPreferenceDto.fromEntity(savedPref.get());
        List<PetMatchResultDto> recommendations = petMatchService.getRecommendations(user, dto);

        model.addAttribute("preferences", dto);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("totalMatches", recommendations.size());
        long highMatchCount = recommendations.stream().filter(r -> r.getCompatibilityScore() >= 75).count();
        model.addAttribute("highMatchCount", highMatchCount);

        return "user/petmatch-results";
    }
}
