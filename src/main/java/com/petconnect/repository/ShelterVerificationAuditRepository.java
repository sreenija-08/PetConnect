package com.petconnect.repository;

import com.petconnect.entity.ShelterVerificationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterVerificationAuditRepository extends JpaRepository<ShelterVerificationAudit, Long> {
    List<ShelterVerificationAudit> findByShelterIdOrderByTimestampDesc(Long shelterId);
}
