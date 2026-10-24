package com.petconnect.repository;

import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByAdoptionStatus(String adoptionStatus);
    List<Pet> findByAdoptionStatusAndVerificationStatus(String adoptionStatus, String verificationStatus);
    List<Pet> findByShelter(Shelter shelter);
    List<Pet> findByShelterOrderByIdDesc(Shelter shelter);
    List<Pet> findByShelterOwner(User owner);
    List<Pet> findByOwnerName(String ownerName);
    List<Pet> findByUser(User user);
    List<Pet> findByUserOrderByIdDesc(User user);
    List<Pet> findAllByOrderByIdDesc();
}
