package com.petconnect.service;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionMeeting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired(required = false)
    private com.petconnect.repository.UserRepository userRepository;

    @Autowired(required = false)
    private com.petconnect.repository.NotificationPreferenceRepository preferenceRepository;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username:}")
    private String mailUsername;

    public EmailServiceImpl() {}

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, "GENERAL");
    }

    private void sendEmail(String to, String subject, String body, String category) {
        if (to == null || to.isBlank()) {
            log.warn("Attempted to send email with empty recipient. Category: {}, Subject: {}", category, subject);
            return;
        }

        if (userRepository != null && preferenceRepository != null) {
            userRepository.findByEmail(to).ifPresent(user -> {
                preferenceRepository.findByUser(user).ifPresent(pref -> {
                    if ("ADOPTION".equalsIgnoreCase(category) && !pref.isEmailAdoptionUpdates()) {
                        log.info("[EMAIL SUPPRESSED] Recipient {} has opted out of adoption emails.", to);
                        return;
                    }
                    if ("VISIT".equalsIgnoreCase(category) && !pref.isEmailVisitUpdates()) {
                        log.info("[EMAIL SUPPRESSED] Recipient {} has opted out of visit emails.", to);
                        return;
                    }
                    if ("CARE".equalsIgnoreCase(category) && !pref.isEmailCareUpdates()) {
                        log.info("[EMAIL SUPPRESSED] Recipient {} has opted out of care emails.", to);
                        return;
                    }
                });
            });
        }

        log.info("\n==================================================" +
                 "\n[EMAIL NOTIFICATION DISPATCH]" +
                 "\nTo: " + to +
                 "\nFrom: " + (mailUsername != null && !mailUsername.isBlank() ? mailUsername : "no-reply@petconnect.com") +
                 "\nCategory: " + category +
                 "\nSubject: " + subject +
                 "\nBody:\n" + body +
                 "\n==================================================");

        if (mailSender == null) {
            log.error("JavaMailSender bean is not available. Check spring.mail configuration in application.properties.");
            if ("SECURITY".equalsIgnoreCase(category)) {
                throw new org.springframework.mail.MailSendException("JavaMailSender is not configured on the server.");
            }
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (mailUsername != null && !mailUsername.isBlank()) {
                message.setFrom(mailUsername);
            } else {
                message.setFrom("no-reply@petconnect.com");
            }
            message.setTo(to.trim());
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email successfully sent via JavaMailSender to {}", to);
        } catch (org.springframework.mail.MailException e) {
            log.error("Failed to send SMTP email to {} [category: {}]: {}", to, category, e.getMessage(), e);
            if ("SECURITY".equalsIgnoreCase(category)) {
                throw e;
            }
        } catch (Exception e) {
            log.error("Unexpected error dispatching email to {} [category: {}]: {}", to, category, e.getMessage(), e);
            if ("SECURITY".equalsIgnoreCase(category)) {
                throw new org.springframework.mail.MailSendException("Unexpected error during email dispatch: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void sendApplicationSubmittedEmail(AdoptionApplication app) {
        String shelterEmail = app.getShelter().getEmail();
        String subject = "New Adoption Application for " + app.getPet().getName();
        String body = "Dear " + app.getShelter().getName() + ",\n\n" +
                "A new adoption application has been submitted for " + app.getPet().getName() + ".\n\n" +
                "Applicant Details:\n" +
                "- Name: " + app.getFullName() + "\n" +
                "- Email: " + app.getEmail() + "\n" +
                "- Phone: " + app.getPhone() + "\n" +
                "- City: " + app.getCity() + "\n\n" +
                "Please log in to your PetConnect shelter portal to review the complete application.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(shelterEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendApplicationShortlistedEmail(AdoptionApplication app) {
        String userEmail = app.getEmail();
        String subject = "Good News! Your Adoption Application for " + app.getPet().getName() + " is Shortlisted";
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Congratulations! Your adoption application for " + app.getPet().getName() + 
                " has been reviewed and SHORTLISTED by " + app.getShelter().getName() + ".\n\n" +
                "Remarks: " + (app.getShelterRemarks() != null ? app.getShelterRemarks() : "Your profile is a great match.") + "\n\n" +
                "Next Step:\n" +
                "Please log in to PetConnect and select an available shelter visit slot or join a scheduled video consultation to meet " + app.getPet().getName() + "!\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendApplicationRejectedEmail(AdoptionApplication app, String remark) {
        String userEmail = app.getEmail();
        String subject = "Update regarding your application for " + app.getPet().getName();
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Thank you for your interest in adopting " + app.getPet().getName() + ". " +
                "After careful review, the shelter was unable to approve this specific adoption application at this time.\n\n" +
                "Shelter Note: " + (remark != null && !remark.isBlank() ? remark : "Another applicant was prioritized or suitability criteria were not met.") + "\n\n" +
                "You are welcome to browse other wonderful pets available on PetConnect.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendVisitScheduledEmail(AdoptionApplication app) {
        String shelterEmail = app.getShelter().getEmail();
        String subject = "Shelter Visit Requested: " + app.getFullName() + " for " + app.getPet().getName();
        String body = "Dear " + app.getShelter().getName() + ",\n\n" +
                "Applicant " + app.getFullName() + " has requested a visit slot to meet " + app.getPet().getName() + ":\n\n" +
                "Requested Slot: " + app.getVisitSlot() + "\n\n" +
                "Please log in to confirm the appointment.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(shelterEmail, subject, body, "VISIT");
    }

    @Override
    public void sendVisitConfirmedEmail(AdoptionApplication app) {
        String userEmail = app.getEmail();
        String subject = "Visit Confirmed! Meeting with " + app.getPet().getName();
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Your visit to " + app.getShelter().getName() + " has been CONFIRMED!\n\n" +
                "Date & Time: " + app.getVisitSlot() + "\n" +
                "Shelter Location: " + app.getShelter().getLocation() + "\n" +
                "Contact: " + app.getShelter().getPhone() + "\n\n" +
                "We hope you and " + app.getPet().getName() + " have a wonderful meeting!\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "VISIT");
    }

    @Override
    public void sendAdminApprovalEmail(AdoptionApplication app) {
        String userEmail = app.getEmail();
        String subject = "Adoption Approved! Final approval for " + app.getPet().getName();
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Exciting news! The PetConnect Administration has officially APPROVED your adoption of " + app.getPet().getName() + "!\n\n" +
                "Admin Remarks: " + (app.getAdminRemarks() != null ? app.getAdminRemarks() : "All verification steps passed successfully.") + "\n\n" +
                "Please coordinate with " + app.getShelter().getName() + " for the pet handover.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendFinalRejectionEmail(AdoptionApplication app, String remark) {
        String userEmail = app.getEmail();
        String subject = "Admin Review Update regarding " + app.getPet().getName();
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Following administrative verification, your application for " + app.getPet().getName() + " could not be approved.\n\n" +
                "Reason: " + (remark != null ? remark : "Did not meet administrative compliance guidelines.") + "\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendAdoptionCompletedEmail(AdoptionApplication app) {
        String userEmail = app.getEmail();
        String subject = "🎉 Congratulations! Adoption of " + app.getPet().getName() + " is Complete!";
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Congratulations on officially welcoming " + app.getPet().getName() + " into your loving forever home!\n\n" +
                "The pet handover has been marked completed by " + app.getShelter().getName() + ".\n\n" +
                "Thank you for saving a life and making a difference.\n\n" +
                "With joy,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "ADOPTION");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String userName, String resetLink) {
        String subject = "PetConnect Password Reset";
        String displayName = (userName != null && !userName.isBlank()) ? userName : "Valued Member";
        String body = "Hello " + displayName + ",\n\n" +
                "We received a request to reset your PetConnect password.\n\n" +
                "Click the link below to create a new password:\n" +
                resetLink + "\n\n" +
                "This link will expire in 30 minutes and can only be used once.\n\n" +
                "If you did not request this password reset, you can safely ignore this email.\n\n" +
                "Warm regards,\nPetConnect Security Team";
        sendEmail(toEmail, subject, body, "SECURITY");
    }

    @Override
    public void sendVideoMeetingScheduledEmail(AdoptionMeeting meeting) {
        if (meeting == null || meeting.getAdoptionApplication() == null) return;
        AdoptionApplication app = meeting.getAdoptionApplication();
        String userEmail = app.getEmail();
        String subject = "📹 Video Consultation Scheduled for " + app.getPet().getName();
        String notesText = (meeting.getNotes() != null && !meeting.getNotes().isBlank()) ? ("\nShelter Notes: " + meeting.getNotes() + "\n") : "";
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "A video consultation meeting has been scheduled with " + app.getShelter().getName() + " regarding your adoption application for " + app.getPet().getName() + ".\n\n" +
                "Meeting Details:\n" +
                "- Date: " + meeting.getFormattedDate() + "\n" +
                "- Time: " + meeting.getFormattedTime() + "\n" +
                "- Duration: " + meeting.getDurationMinutes() + " Minutes\n" +
                notesText + "\n" +
                "You can join the video consultation directly from your PetConnect Adoption Tracker page or by clicking 'Join Video Call' in your dashboard.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "VISIT");
    }

    @Override
    public void sendVideoMeetingRescheduledEmail(AdoptionMeeting meeting) {
        if (meeting == null || meeting.getAdoptionApplication() == null) return;
        AdoptionApplication app = meeting.getAdoptionApplication();
        String userEmail = app.getEmail();
        String subject = "🔄 Video Consultation Rescheduled for " + app.getPet().getName();
        String notesText = (meeting.getNotes() != null && !meeting.getNotes().isBlank()) ? ("\nShelter Notes: " + meeting.getNotes() + "\n") : "";
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "Your video consultation meeting with " + app.getShelter().getName() + " for " + app.getPet().getName() + " has been rescheduled.\n\n" +
                "New Meeting Details:\n" +
                "- Date: " + meeting.getFormattedDate() + "\n" +
                "- Time: " + meeting.getFormattedTime() + "\n" +
                "- Duration: " + meeting.getDurationMinutes() + " Minutes\n" +
                notesText + "\n" +
                "Please log in to your PetConnect dashboard to view details and join when the meeting starts.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "VISIT");
    }

    @Override
    public void sendVideoMeetingCancelledEmail(AdoptionMeeting meeting, String reason) {
        if (meeting == null || meeting.getAdoptionApplication() == null) return;
        AdoptionApplication app = meeting.getAdoptionApplication();
        String userEmail = app.getEmail();
        String subject = "Video Consultation Cancelled for " + app.getPet().getName();
        String reasonText = (reason != null && !reason.isBlank()) ? ("\nReason: " + reason + "\n") : "";
        String body = "Dear " + app.getFullName() + ",\n\n" +
                "The scheduled video consultation for " + app.getPet().getName() + " on " + meeting.getFormattedDate() + " at " + meeting.getFormattedTime() + " has been cancelled." +
                reasonText + "\n" +
                "Please check your PetConnect dashboard or contact the shelter for further information.\n\n" +
                "Warm regards,\nPetConnect Team";
        sendEmail(userEmail, subject, body, "VISIT");
    }

    @Override
    public void sendShelterVerificationEmail(String toEmail, String shelterName, String subject, String messageText) {
        sendEmail(toEmail, subject, messageText, "SHELTER");
    }
}
