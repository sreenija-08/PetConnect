package com.petconnect.repository;

import com.petconnect.entity.Shelter;
import com.petconnect.entity.ShelterImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShelterImageRepository extends JpaRepository<ShelterImage, Long> {
    List<ShelterImage> findByShelterOrderByIdAsc(Shelter shelter);
    void deleteByShelter(Shelter shelter);
}
