package com.petconnect.repository;

import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShelterDocumentRepository extends JpaRepository<ShelterDocument, Long> {
    List<ShelterDocument> findByShelterOrderByCreatedAtDesc(Shelter shelter);
    Optional<ShelterDocument> findFirstByShelterOrderByCreatedAtDesc(Shelter shelter);
    List<ShelterDocument> findByShelterIdOrderByCreatedAtDesc(Long shelterId);
    Optional<ShelterDocument> findFirstByShelterIdOrderByCreatedAtDesc(Long shelterId);
}
