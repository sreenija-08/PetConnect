package com.petconnect.repository;

import com.petconnect.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndPetId(Long userId, Long petId);

    boolean existsByUserIdAndPetId(Long userId, Long petId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("SELECT f.pet.id FROM Favorite f WHERE f.user.id = :userId")
    Set<Long> findFavoritePetIdsByUserId(@Param("userId") Long userId);

    @Transactional
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.pet.id = :petId")
    void deleteByUserIdAndPetId(@Param("userId") Long userId, @Param("petId") Long petId);

    long countByUserId(Long userId);
}
