package com.petconnect.repository;

import com.petconnect.entity.User;
import com.petconnect.entity.UserPetPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPetPreferenceRepository extends JpaRepository<UserPetPreference, Long> {
    Optional<UserPetPreference> findByUser(User user);
    Optional<UserPetPreference> findByUserId(Long userId);
}
