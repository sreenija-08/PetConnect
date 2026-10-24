package com.petconnect.service;

import com.petconnect.entity.Pet;

import java.util.List;
import java.util.Set;

public interface FavoriteService {

    boolean toggleFavorite(Long userId, Long petId);

    void removeFavorite(Long userId, Long petId);

    boolean isFavorite(Long userId, Long petId);

    Set<Long> getFavoritePetIdsForUser(Long userId);

    List<Pet> getFavoritePetsForUser(Long userId);

    long getFavoriteCountForUser(Long userId);
}
