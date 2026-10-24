package com.petconnect.controller;

import com.petconnect.dto.ShelterReviewDto;
import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionStatus;
import com.petconnect.entity.ShelterReview;
import com.petconnect.entity.User;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.ShelterReviewService;
import com.petconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class ShelterReviewController {

    private final ShelterReviewService reviewService;
    private final AdoptionService adoptionService;
    private final UserService userService;

    @Autowired
    public ShelterReviewController(ShelterReviewService reviewService,
                                   AdoptionService adoptionService,
                                   UserService userService) {
        this.reviewService = reviewService;
        this.adoptionService = adoptionService;
        this.userService = userService;
    }

    @ModelAttribute
    public void addLoggedInUser(Model model, Principal principal) {
        if (principal != null) {
            User user = userService.findByEmail(principal.getName());
            model.addAttribute("loggedInUser", user);
        }
    }

    // 1. User Opens Review Form for Completed Adoption
    @GetMapping({"/user/shelter-review/{id}", "/user/adoption-requests/{id}/review"})
    public String showReviewForm(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        AdoptionApplication application = adoptionService.getApplicationById(id);

        if (application == null || !application.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adoption request not found or access denied.");
            return "redirect:/user/adoption-requests";
        }

        // Must be COMPLETED
        if (application.getStatus() != AdoptionStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter reviews can only be submitted after the adoption is officially completed.");
            return "redirect:/user/adoption-requests/" + id;
        }

        // Already reviewed?
        Optional<ShelterReview> existing = reviewService.getReviewForApplication(id);
        if (existing.isPresent()) {
            redirectAttributes.addFlashAttribute("infoMessage", "You have already reviewed this shelter for this adoption.");
            return "redirect:/user/adoption-requests/" + id;
        }

        ShelterReviewDto form = new ShelterReviewDto();
        form.setAdoptionRequestId(id);
        form.setRating(null); // No rating pre-selected

        model.addAttribute("adoptionApplication", application);
        model.addAttribute("app", application);
        model.addAttribute("pet", application.getPet());
        model.addAttribute("shelter", application.getShelter());
        model.addAttribute("reviewForm", form);

        return "user/review-shelter";
    }

    // 2. User Submits Review Form
    @PostMapping({"/user/shelter-review/{id}", "/user/adoption-requests/{id}/review"})
    public String submitReview(@PathVariable Long id,
                               @ModelAttribute("reviewForm") @Valid ShelterReviewDto reviewForm,
                               BindingResult bindingResult,
                               Model model,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        AdoptionApplication application = adoptionService.getApplicationById(id);

        reviewForm.setAdoptionRequestId(id);
        if (application == null || !application.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adoption request not found or access denied.");
            return "redirect:/user/adoption-requests";
        }

        if (application.getStatus() != AdoptionStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("errorMessage", "Shelter reviews can only be submitted after the adoption is officially completed.");
            return "redirect:/user/adoption-requests/" + id;
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("adoptionApplication", application);
            model.addAttribute("app", application);
            model.addAttribute("pet", application.getPet());
            model.addAttribute("shelter", application.getShelter());
            return "user/review-shelter";
        }

        try {
            reviewService.submitReview(user, id, reviewForm);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Thank you! Your review for " + application.getShelter().getName() + " has been submitted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/adoption-requests/" + id;
    }

    // 3. User's "My Reviews" List Page
    @GetMapping("/user/reviews")
    public String myReviews(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        List<ShelterReview> reviews = reviewService.getReviewsByUser(user);
        model.addAttribute("reviews", reviews);

        return "user/my-reviews";
    }

    // 4. Admin View for All Shelter Reviews
    @GetMapping("/admin/shelter-reviews")
    public String adminShelterReviews(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        List<ShelterReview> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);

        return "admin/shelter-reviews";
    }
}
