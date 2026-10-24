package com.petconnect.repository;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionMeetingRepository extends JpaRepository<AdoptionMeeting, Long> {

    List<AdoptionMeeting> findByAdoptionApplicationOrderByMeetingDateDescMeetingTimeDesc(AdoptionApplication adoptionApplication);

    List<AdoptionMeeting> findByAdoptionApplicationOrderByCreatedAtDesc(AdoptionApplication adoptionApplication);

    List<AdoptionMeeting> findByAdoptionApplicationAndStatusOrderByCreatedAtDesc(AdoptionApplication adoptionApplication, String status);

    Optional<AdoptionMeeting> findTopByAdoptionApplicationAndStatusOrderByCreatedAtDesc(AdoptionApplication adoptionApplication, String status);

    Optional<AdoptionMeeting> findTopByAdoptionApplicationOrderByCreatedAtDesc(AdoptionApplication adoptionApplication);

    Optional<AdoptionMeeting> findByRoomName(String roomName);

    List<AdoptionMeeting> findByAdoptionApplication(AdoptionApplication adoptionApplication);
}
