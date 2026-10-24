package com.petconnect.service;

import com.petconnect.dto.PetMatchPreferenceDto;
import com.petconnect.dto.PetMatchResultDto;
import com.petconnect.entity.Pet;
import com.petconnect.entity.User;
import com.petconnect.entity.UserPetPreference;

import java.util.List;
import java.util.Optional;

public interface PetMatchService {

    /**
     * Recommends eligible, approved, and available pets for the specified user and preferences,
     * ranked descending by compatibility score (0-100).
     */
    List<PetMatchResultDto> getRecommendations(User user, PetMatchPreferenceDto preferences);

    /**
     * Calculates compatibility score (0-100) and builds dynamic match explanations for a specific pet.
     */
    PetMatchResultDto calculateCompatibility(PetMatchPreferenceDto preferences, Pet pet, boolean isFavorite);

    /**
     * Retrieves saved preferences for the user if any.
     */
    Optional<UserPetPreference> getUserPreferences(User user);

    /**
     * Persists or updates user preferences.
     */
    UserPetPreference saveUserPreferences(User user, PetMatchPreferenceDto preferences);
}
