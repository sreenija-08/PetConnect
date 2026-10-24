package com.petconnect.controller;

import com.petconnect.dto.AdoptionFormDto;
import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.Pet;
import com.petconnect.entity.User;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.PetService;
import com.petconnect.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class AdoptionController {

    private final PetService petService;
    private final AdoptionService adoptionService;
    private final UserService userService;

    @Autowired
    public AdoptionController(PetService petService,
                              AdoptionService adoptionService,
                              UserService userService) {
        this.petService = petService;
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

    // Step 0: Readiness Checklist
    @GetMapping("/pet/{id}/adopt/readiness")
    public String readinessCheck(@PathVariable Long id, Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Pet pet = petService.getPetById(id);
        if (pet == null || !"AVAILABLE".equalsIgnoreCase(pet.getAdoptionStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This pet is currently unavailable for adoption.");
            return "redirect:/browse-pets";
        }

        User user = userService.findByEmail(principal.getName());
        if (adoptionService.hasActiveApplication(user, pet)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You already have an active adoption application for " + pet.getName() + ".");
            return "redirect:/user/adoption-requests";
        }

        model.addAttribute("pet", pet);
        return "public/adoption-readiness";
    }

    // Multi-Step Application Wizard
    @GetMapping("/pet/{id}/adopt/apply")
    public String showApplicationWizard(@PathVariable Long id,
                                        @RequestParam(name = "q1", required = false) String q1,
                                        @RequestParam(name = "q2", required = false) String q2,
                                        @RequestParam(name = "q3", required = false) String q3,
                                        @RequestParam(name = "q4", required = false) String q4,
                                        Model model,
                                        Principal principal,
                                        RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Pet pet = petService.getPetById(id);
        if (pet == null || !"AVAILABLE".equalsIgnoreCase(pet.getAdoptionStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "This pet is currently unavailable for adoption.");
            return "redirect:/browse-pets";
        }

        User user = userService.findByEmail(principal.getName());
        if (adoptionService.hasActiveApplication(user, pet)) {
            redirectAttributes.addFlashAttribute("errorMessage", "You already have an active application for " + pet.getName() + ".");
            return "redirect:/user/adoption-requests";
        }

        // Validate readiness questions
        if (q1 == null || q1.isBlank() || q2 == null || q2.isBlank() || q3 == null || q3.isBlank() || q4 == null || q4.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please answer all readiness questions before proceeding.");
            return "redirect:/pet/" + id + "/adopt/readiness";
        }

        AdoptionFormDto form = new AdoptionFormDto();
        form.setPetId(pet.getId());
        form.setReadinessQ1(q1);
        form.setReadinessQ2(q2);
        form.setReadinessQ3(q3);
        form.setReadinessQ4(q4);
        form.setFullName(user.getFullName() != null ? user.getFullName() : "");
        form.setEmail(user.getEmail() != null ? user.getEmail() : "");
        form.setPhone(user.getPhone() != null ? user.getPhone() : "");
        if (user.getCity() != null && !user.getCity().isBlank()) {
            form.setCity(user.getCity());
        } else {
            var pastApps = adoptionService.getUserApplications(user);
            if (!pastApps.isEmpty() && pastApps.get(0).getCity() != null) {
                form.setCity(pastApps.get(0).getCity());
            }
        }

        model.addAttribute("pet", pet);
        model.addAttribute("shelter", pet.getShelter());
        model.addAttribute("form", form);
        return "public/adoption-wizard";
    }

    // Submit Application Form POST
    @PostMapping("/pet/{id}/adopt/submit")
    public String submitApplication(@PathVariable Long id,
                                    @ModelAttribute("form") @Valid AdoptionFormDto form,
                                    BindingResult result,
                                    Model model,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Pet pet = petService.getPetById(id);
        if (pet == null || !"AVAILABLE".equalsIgnoreCase(pet.getAdoptionStatus())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Pet is no longer available.");
            return "redirect:/browse-pets";
        }

        User user = userService.findByEmail(principal.getName());

        if (result.hasErrors()) {
            model.addAttribute("pet", pet);
            model.addAttribute("shelter", pet.getShelter());
            return "public/adoption-wizard";
        }

        // Keep user phone and city synchronized
        if (form.getPhone() != null && !form.getPhone().isBlank() && (user.getPhone() == null || user.getPhone().isBlank())) {
            user.setPhone(form.getPhone());
            userService.save(user);
        }
        if (form.getCity() != null && !form.getCity().isBlank() && (user.getCity() == null || user.getCity().isBlank())) {
            user.setCity(form.getCity());
            userService.save(user);
        }

        try {
            adoptionService.submitApplication(user, id, form);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Application submitted successfully for " + pet.getName() + "! The shelter will review it shortly.");
            return "redirect:/user/adoption-requests";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/adoption-requests";
        }
    }

    // Applicant Schedules Visit Slot
    @PostMapping("/user/adoption-requests/{id}/schedule-visit")
    public String scheduleVisitSlot(@PathVariable Long id,
                                    @RequestParam(value = "slotId", required = false) Long slotId,
                                    @RequestParam(value = "visitSlot", required = false) String visitSlot,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";

        User user = userService.findByEmail(principal.getName());
        try {
            if (slotId != null) {
                adoptionService.requestVisitSlotById(id, slotId, user);
            } else if (visitSlot != null && !visitSlot.isBlank()) {
                adoptionService.scheduleVisit(id, visitSlot, user);
            } else {
                throw new IllegalArgumentException("Please select a valid visit slot.");
            }
            redirectAttributes.addFlashAttribute("successMessage", "Visit slot requested! The shelter will confirm shortly.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/user/adoption-requests/" + id;
    }
}
