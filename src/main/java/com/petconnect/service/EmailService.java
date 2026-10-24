package com.petconnect.service;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionMeeting;

public interface EmailService {
    void sendApplicationSubmittedEmail(AdoptionApplication app);
    void sendApplicationShortlistedEmail(AdoptionApplication app);
    void sendApplicationRejectedEmail(AdoptionApplication app, String remark);
    void sendVisitScheduledEmail(AdoptionApplication app);
    void sendVisitConfirmedEmail(AdoptionApplication app);
    void sendAdminApprovalEmail(AdoptionApplication app);
    void sendFinalRejectionEmail(AdoptionApplication app, String remark);
    void sendAdoptionCompletedEmail(AdoptionApplication app);
    void sendPasswordResetEmail(String toEmail, String userName, String resetLink);
    
    // Video Meeting Emails
    void sendVideoMeetingScheduledEmail(AdoptionMeeting meeting);
    void sendVideoMeetingRescheduledEmail(AdoptionMeeting meeting);
    void sendVideoMeetingCancelledEmail(AdoptionMeeting meeting, String reason);

    // Shelter Document Verification Emails
    void sendShelterVerificationEmail(String toEmail, String shelterName, String subject, String messageText);
}
