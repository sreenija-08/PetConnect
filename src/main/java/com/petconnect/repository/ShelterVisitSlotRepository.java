package com.petconnect.repository;

import com.petconnect.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterVisitSlotRepository extends JpaRepository<ShelterVisitSlot, Long> {

    List<ShelterVisitSlot> findByShelter(Shelter shelter);

    List<ShelterVisitSlot> findByShelterAndStatus(Shelter shelter, String status);

    List<ShelterVisitSlot> findByApplication(AdoptionApplication application);

    List<ShelterVisitSlot> findByApplicationId(Long applicationId);

    Optional<ShelterVisitSlot> findByShelterAndVisitDateAndTimeSlot(Shelter shelter, LocalDate visitDate, String timeSlot);

    List<ShelterVisitSlot> findByShelterAndStatusIn(Shelter shelter, List<String> statuses);

    List<ShelterVisitSlot> findByShelterAndStatusOrderByVisitDateAsc(Shelter shelter, String status);

    List<ShelterVisitSlot> findByBookedBy(User bookedBy);
}
