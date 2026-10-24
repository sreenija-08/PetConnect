package com.petconnect.service;

import com.petconnect.entity.AdoptionMeeting;
import com.petconnect.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AdoptionMeetingService {

    AdoptionMeeting scheduleMeeting(Long applicationId, LocalDate meetingDate, LocalTime meetingTime, Integer durationMinutes, String notes, User shelterOwner);

    AdoptionMeeting rescheduleMeeting(Long meetingId, LocalDate newDate, LocalTime newTime, Integer durationMinutes, String notes, User shelterOwner);

    AdoptionMeeting cancelMeeting(Long meetingId, String reason, User user);

    AdoptionMeeting markMeetingCompleted(Long meetingId, User user);

    Optional<AdoptionMeeting> getActiveMeetingForApplication(Long applicationId);

    List<AdoptionMeeting> getMeetingsForApplication(Long applicationId);

    AdoptionMeeting getMeetingById(Long meetingId);

    boolean canUserAccessMeeting(AdoptionMeeting meeting, User user);
}
