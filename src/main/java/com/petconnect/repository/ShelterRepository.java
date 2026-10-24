package com.petconnect.repository;

import com.petconnect.entity.Shelter;
import com.petconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, Long> {
    List<Shelter> findByVerificationStatus(String status);
    Optional<Shelter> findByName(String name);
    List<Shelter> findByOwner(User owner);
    Optional<Shelter> findFirstByOwner(User owner);
}
