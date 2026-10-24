package com.petconnect.repository;

import com.petconnect.entity.CareBooking;
import com.petconnect.entity.CareBookingStatus;
import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface CareBookingRepository extends JpaRepository<CareBooking, Long> {

    List<CareBooking> findByUserOrderByRequestedAtDesc(User user);

    List<CareBooking> findByShelterOrderByRequestedAtDesc(Shelter shelter);

    List<CareBooking> findByPet(Pet pet);

    List<CareBooking> findByPetId(Long petId);

    List<CareBooking> findByShelterAndStatusInOrderByRequestedAtDesc(Shelter shelter, Collection<CareBookingStatus> statuses);

    @Query("SELECT b FROM CareBooking b WHERE b.shelter = :shelter AND b.status IN (:statuses) AND b.startDate <= :endDate AND b.endDate >= :startDate")
    List<CareBooking> findOverlappingShelterBookings(
            @Param("shelter") Shelter shelter,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<CareBookingStatus> statuses
    );

    @Query("SELECT b FROM CareBooking b WHERE b.pet = :pet AND b.status IN (:statuses) AND b.startDate <= :endDate AND b.endDate >= :startDate")
    List<CareBooking> findOverlappingPetBookings(
            @Param("pet") Pet pet,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") Collection<CareBookingStatus> statuses
    );
}
