package com.petconnect.service;

import com.petconnect.dto.AdoptionFormDto;
import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.Pet;
import com.petconnect.entity.User;

import java.util.List;

public interface AdoptionService {
    AdoptionApplication submitApplication(User applicant, Long petId, AdoptionFormDto formDto);
    boolean hasActiveApplication(User user, Pet pet);
    List<AdoptionApplication> getUserApplications(User user);
    List<AdoptionApplication> getShelterApplications(User shelterOwner);
    List<AdoptionApplication> getApplicationsByShelter(com.petconnect.entity.Shelter shelter);
    List<AdoptionApplication> getAdminApplications();
    AdoptionApplication getApplicationById(Long id);

    // Workflow Actions
    AdoptionApplication shortlistApplication(Long id, String remarks, User operator);
    AdoptionApplication rejectByShelter(Long id, String remarks, User operator);
    List<com.petconnect.entity.ShelterVisitSlot> createVisitSlots(Long applicationId, java.time.LocalDate visitDate, List<String> times, User shelterOwner);
    List<com.petconnect.entity.ShelterVisitSlot> getAvailableSlotsForApplication(Long applicationId);
    List<com.petconnect.entity.ShelterVisitSlot> getSlotsForApplication(Long applicationId);
    AdoptionApplication requestVisitSlotById(Long applicationId, Long slotId, User applicant);
    AdoptionApplication scheduleVisit(Long id, String slot, User applicant);
    AdoptionApplication confirmVisit(Long id, User operator);
    AdoptionApplication rescheduleVisit(Long id, String remarks, User operator);
    AdoptionApplication rejectVisit(Long id, String remarks, User operator);
    AdoptionApplication markVisitCompleted(Long id, User operator);
    AdoptionApplication submitRecommendation(Long id, boolean recommended, String notes, User operator);
    AdoptionApplication adminReview(Long id, boolean approved, String remarks, User admin);
    AdoptionApplication completeHandover(Long id, User operator);
}
