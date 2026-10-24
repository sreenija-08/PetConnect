package com.petconnect.repository;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterReview;
import com.petconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterReviewRepository extends JpaRepository<ShelterReview, Long> {

    Optional<ShelterReview> findByAdoptionApplication(AdoptionApplication adoptionApplication);

    Optional<ShelterReview> findByAdoptionApplicationId(Long adoptionApplicationId);

    boolean existsByAdoptionApplication(AdoptionApplication adoptionApplication);

    boolean existsByAdoptionApplicationId(Long adoptionApplicationId);

    List<ShelterReview> findByUserOrderByCreatedAtDesc(User user);

    List<ShelterReview> findByShelterOrderByCreatedAtDesc(Shelter shelter);

    List<ShelterReview> findByShelterIdOrderByCreatedAtDesc(Long shelterId);

    List<ShelterReview> findAllByOrderByCreatedAtDesc();
}
