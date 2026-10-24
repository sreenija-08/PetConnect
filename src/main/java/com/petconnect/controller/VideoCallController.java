package com.petconnect.controller;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionMeeting;
import com.petconnect.entity.User;
import com.petconnect.service.AdoptionMeetingService;
import com.petconnect.service.AdoptionService;
import com.petconnect.service.NotificationService;
import com.petconnect.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class VideoCallController {

    private final AdoptionMeetingService meetingService;
    private final AdoptionService adoptionService;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public VideoCallController(AdoptionMeetingService meetingService,
                               AdoptionService adoptionService,
                               UserService userService,
                               NotificationService notificationService) {
        this.meetingService = meetingService;
        this.adoptionService = adoptionService;
        this.userService = userService;
        this.notificationService = notificationService;
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
     * Dedicated secure video call room endpoint.
     * Enforces strict authorization and server-side schedule window checks.
     */
    @GetMapping("/video-call/{meetingId}")
    public String joinVideoCall(@PathVariable Long meetingId,
                                Model model,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        AdoptionMeeting meeting;
        try {
            meeting = meetingService.getMeetingById(meetingId);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Video meeting not found.");
            return "redirect:/user/dashboard";
        }

        AdoptionApplication app = meeting.getAdoptionApplication();
        if (app == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Application associated with this video call was not found.");
            return "redirect:/user/dashboard";
        }

        // 1. Strict Authorization Check (Applicant, Shelter Owner, or Admin only)
        if (!meetingService.canUserAccessMeeting(meeting, user)) {
            throw new AccessDeniedException("Access Denied: You do not have permission to join this video consultation.");
        }

        // Determine user role and return URL
        boolean isOwner = false;
        if (user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "ROLE_SHELTER_OWNER".equalsIgnoreCase(r.getName()) || "SHELTER_OWNER".equalsIgnoreCase(r.getName()))) {
            isOwner = true;
        } else if (app.getShelter() != null && app.getShelter().getOwner() != null && app.getShelter().getOwner().getId().equals(user.getId())) {
            isOwner = true;
        }

        String returnUrl = isOwner ? ("/owner/adoption-requests/" + app.getId()) : ("/user/adoption-requests/" + app.getId());

        // 2. Check if meeting was cancelled
        if (meeting.isCancelled()) {
            redirectAttributes.addFlashAttribute("errorMessage", "This video consultation was cancelled. " + (meeting.getCancellationReason() != null ? "Reason: " + meeting.getCancellationReason() : ""));
            return "redirect:" + returnUrl;
        }

        // 3. Server-side schedule lifecycle evaluation (Asia/Kolkata)
        String meetingState = meeting.getMeetingLifecycleState(); // "SCHEDULED", "LIVE", "COMPLETED"
        boolean canEnterCall = meeting.isDuringMeetingWindow();
        boolean isBeforeMeeting = meeting.isBeforeMeetingWindow();
        boolean isAfterMeeting = meeting.isAfterMeetingWindow();

        String roleTag = isOwner ? " (Shelter Owner - Host)" : " (Applicant)";
        String userDisplayName = (user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getEmail()) + roleTag;

        model.addAttribute("meeting", meeting);
        model.addAttribute("meetingState", meetingState);
        model.addAttribute("canEnterCall", canEnterCall);
        model.addAttribute("isBeforeMeeting", isBeforeMeeting);
        model.addAttribute("isAfterMeeting", isAfterMeeting);
        model.addAttribute("adoptionApplication", app);
        model.addAttribute("app", app);
        model.addAttribute("pet", app.getPet());
        model.addAttribute("shelter", app.getShelter());
        model.addAttribute("roomName", meeting.getRoomName());
        model.addAttribute("userDisplayName", userDisplayName);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("isShelterOwner", isOwner);
        model.addAttribute("jitsiDomain", "meet.jit.si");

        return "public/video-call";
    }

    /**
     * Convenience route from an adoption application directly to its active video call.
     */
    @GetMapping("/adoptions/{id}/video-call")
    public String joinApplicationVideoCall(@PathVariable Long id,
                                           Principal principal,
                                           RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }

        AdoptionApplication app;
        try {
            app = adoptionService.getApplicationById(id);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Adoption application not found.");
            return "redirect:/user/dashboard";
        }

        Optional<AdoptionMeeting> activeMeeting = meetingService.getActiveMeetingForApplication(id);
        if (activeMeeting.isPresent()) {
            return "redirect:/video-call/" + activeMeeting.get().getId();
        }

        // If no active meeting found
        boolean isOwner = (user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "ROLE_SHELTER_OWNER".equalsIgnoreCase(r.getName())));
        if (isOwner) {
            redirectAttributes.addFlashAttribute("errorMessage", "No active video meeting found. Please schedule a consultation first.");
            return "redirect:/owner/adoption-requests/" + id;
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "No active video consultation is currently scheduled by the shelter.");
            return "redirect:/user/adoption-requests/" + id;
        }
    }

    /**
     * Marks video call completed when participants finish the meeting.
     */
    @PostMapping("/video-call/{meetingId}/complete")
    public String completeVideoCall(@PathVariable Long meetingId,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getName());
        try {
            AdoptionMeeting meeting = meetingService.markMeetingCompleted(meetingId, user);
            redirectAttributes.addFlashAttribute("successMessage", "Video consultation concluded successfully.");
            boolean isOwner = (user.getRoles() != null && user.getRoles().stream().anyMatch(r -> "ROLE_SHELTER_OWNER".equalsIgnoreCase(r.getName())));
            return isOwner ? ("redirect:/owner/adoption-requests/" + meeting.getAdoptionApplication().getId()) :
                             ("redirect:/user/adoption-requests/" + meeting.getAdoptionApplication().getId());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/user/dashboard";
        }
    }
}
