package com.petconnect.service;

import com.petconnect.entity.Favorite;
import com.petconnect.entity.Pet;
import com.petconnect.entity.User;
import com.petconnect.repository.FavoriteRepository;
import com.petconnect.repository.PetRepository;
import com.petconnect.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final PetRepository petRepository;

    @Autowired
    public FavoriteServiceImpl(FavoriteRepository favoriteRepository,
                               UserRepository userRepository,
                               PetRepository petRepository) {
        this.favoriteRepository = favoriteRepository;
        this.userRepository = userRepository;
        this.petRepository = petRepository;
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long petId) {
        if (userId == null || petId == null) {
            return false;
        }

        Optional<Favorite> existing = favoriteRepository.findByUserIdAndPetId(userId, petId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return false; // Removed from favorites
        }

        User user = userRepository.findById(userId).orElse(null);
        Pet pet = petRepository.findById(petId).orElse(null);

        if (user != null && pet != null) {
            Favorite favorite = new Favorite(user, pet);
            favoriteRepository.save(favorite);
            return true; // Added to favorites
        }

        return false;
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long petId) {
        if (userId != null && petId != null) {
            favoriteRepository.deleteByUserIdAndPetId(userId, petId);
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long petId) {
        if (userId == null || petId == null) {
            return false;
        }
        return favoriteRepository.existsByUserIdAndPetId(userId, petId);
    }

    @Override
    public Set<Long> getFavoritePetIdsForUser(Long userId) {
        if (userId == null) {
            return Collections.emptySet();
        }
        return favoriteRepository.findFavoritePetIdsByUserId(userId);
    }

    @Override
    public List<Pet> getFavoritePetsForUser(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<Favorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return favorites.stream()
                .map(Favorite::getPet)
                .filter(pet -> pet != null
                        && !"REMOVED".equalsIgnoreCase(pet.getAdoptionStatus())
                        && !"INACTIVE".equalsIgnoreCase(pet.getAdoptionStatus())
                        && !"INACTIVE".equalsIgnoreCase(pet.getVerificationStatus())
                        && (pet.getShelter() == null || (pet.getShelter().isActive() && !"REJECTED".equalsIgnoreCase(pet.getShelter().getVerificationStatus()))))
                .collect(Collectors.toList());
    }

    @Override
    public long getFavoriteCountForUser(Long userId) {
        if (userId == null) {
            return 0;
        }
        return getFavoritePetsForUser(userId).size();
    }
}
