package com.petconnect.service;

import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;

import com.petconnect.entity.User;

import java.util.List;

public interface PetService {
    List<Pet> getAvailablePets();
    Pet getPetById(Long id);
    List<Pet> filterPets(String search, String type, String breed, String gender, Integer age);
    List<String> getAvailableBreeds();
    List<String> getAvailableBreedsByType(String type);
    List<Shelter> getAllShelters();
    Shelter getShelterById(Long id);
    Pet savePet(Pet pet);
    List<Pet> getPetsByShelter(Shelter shelter);
    List<Pet> getAllPetsForAdmin();
    Pet verifyPet(Long petId, String status);
    Pet updatePet(Long petId, Pet updatedData, User owner);
    String deletePet(Long petId, User owner);
}
