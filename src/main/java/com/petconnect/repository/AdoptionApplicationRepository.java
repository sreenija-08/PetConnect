package com.petconnect.repository;

import com.petconnect.entity.AdoptionApplication;
import com.petconnect.entity.AdoptionStatus;
import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    List<AdoptionApplication> findByUserOrderByCreatedAtDesc(User user);
    List<AdoptionApplication> findByShelterOrderByCreatedAtDesc(Shelter shelter);
    List<AdoptionApplication> findByShelterIdOrderByCreatedAtDesc(Long shelterId);
    List<AdoptionApplication> findByShelterInOrderByCreatedAtDesc(Collection<Shelter> shelters);
    List<AdoptionApplication> findByShelterOwnerOrderByCreatedAtDesc(User owner);
    List<AdoptionApplication> findByStatusOrderByCreatedAtDesc(AdoptionStatus status);
    List<AdoptionApplication> findByStatusInOrderByCreatedAtDesc(Collection<AdoptionStatus> statuses);
    List<AdoptionApplication> findAllByOrderByCreatedAtDesc();
    List<AdoptionApplication> findByPet(Pet pet);
    List<AdoptionApplication> findByPetId(Long petId);
    boolean existsByUserAndPetAndStatusNotIn(User user, Pet pet, Collection<AdoptionStatus> terminalStatuses);
}
