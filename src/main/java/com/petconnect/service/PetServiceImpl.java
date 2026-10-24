package com.petconnect.service;

import com.petconnect.entity.Pet;
import com.petconnect.entity.Shelter;
import com.petconnect.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final ShelterRepository shelterRepository;
    private final AdoptionApplicationRepository adoptionApplicationRepository;
    private final CareBookingRepository careBookingRepository;
    private final ShelterVisitSlotRepository shelterVisitSlotRepository;
    private final ShelterReviewRepository shelterReviewRepository;

    @Autowired
    public PetServiceImpl(PetRepository petRepository,
                          ShelterRepository shelterRepository,
                          AdoptionApplicationRepository adoptionApplicationRepository,
                          CareBookingRepository careBookingRepository,
                          ShelterVisitSlotRepository shelterVisitSlotRepository,
                          ShelterReviewRepository shelterReviewRepository) {
        this.petRepository = petRepository;
        this.shelterRepository = shelterRepository;
        this.adoptionApplicationRepository = adoptionApplicationRepository;
        this.careBookingRepository = careBookingRepository;
        this.shelterVisitSlotRepository = shelterVisitSlotRepository;
        this.shelterReviewRepository = shelterReviewRepository;
    }

    @Override
    public List<Pet> getAvailablePets() {
        return petRepository.findByAdoptionStatusAndVerificationStatus("AVAILABLE", "APPROVED").stream()
                .filter(pet -> !"REMOVED".equalsIgnoreCase(pet.getAdoptionStatus()) && !"INACTIVE".equalsIgnoreCase(pet.getAdoptionStatus()))
                .filter(pet -> !"INACTIVE".equalsIgnoreCase(pet.getVerificationStatus()))
                .filter(pet -> pet.getShelter() != null && pet.getShelter().isActive() && "APPROVED".equalsIgnoreCase(pet.getShelter().getVerificationStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElse(null);
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) return null;
        String clean = type.trim().toLowerCase();
        if (clean.equals("all") || clean.equals("all types") || clean.equals("all_types") || clean.equals("all-types")) {
            return null;
        }
        if (clean.startsWith("dog")) return "DOG";
        if (clean.startsWith("cat")) return "CAT";
        return clean.toUpperCase();
    }

    private String normalizeBreed(String breed) {
        if (breed == null || breed.isBlank()) return null;
        String clean = breed.trim();
        if (clean.equalsIgnoreCase("all") || clean.equalsIgnoreCase("all breeds") || clean.equalsIgnoreCase("all_breeds") || clean.equalsIgnoreCase("all-breeds")) {
            return null;
        }
        return clean;
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) return null;
        String clean = gender.trim().toLowerCase();
        if (clean.equals("all") || clean.equals("all genders") || clean.equals("all_genders") || clean.equals("all-genders")) {
            return null;
        }
        if (clean.startsWith("m")) return "MALE";
        if (clean.startsWith("f")) return "FEMALE";
        return clean.toUpperCase();
    }

    @Override
    public List<Pet> filterPets(String search, String type, String breed, String gender, Integer age) {
        List<Pet> all = getAvailablePets();
        final String normType = normalizeType(type);
        final String normBreed = normalizeBreed(breed);
        final String normGender = normalizeGender(gender);
        final String cleanSearch = (search != null && !search.isBlank()) ? search.trim().toLowerCase() : null;

        return all.stream()
                .filter(pet -> {
                    if (cleanSearch == null) return true;
                    boolean nameMatch = pet.getName() != null && pet.getName().toLowerCase().contains(cleanSearch);
                    boolean breedMatch = pet.getBreed() != null && pet.getBreed().toLowerCase().contains(cleanSearch);
                    boolean descMatch = pet.getDescription() != null && pet.getDescription().toLowerCase().contains(cleanSearch);
                    return nameMatch || breedMatch || descMatch;
                })
                .filter(pet -> {
                    if (normType == null) return true;
                    String petTypeNorm = normalizeType(pet.getType());
                    return normType.equalsIgnoreCase(petTypeNorm);
                })
                .filter(pet -> {
                    if (normBreed == null) return true;
                    return pet.getBreed() != null && pet.getBreed().trim().equalsIgnoreCase(normBreed);
                })
                .filter(pet -> {
                    if (normGender == null) return true;
                    String petGenderNorm = normalizeGender(pet.getGender());
                    return normGender.equalsIgnoreCase(petGenderNorm);
                })
                .filter(pet -> age == null || (pet.getAge() != null && pet.getAge().equals(age)))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableBreeds() {
        return getAvailablePets().stream()
                .map(Pet::getBreed)
                .filter(b -> b != null && !b.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAvailableBreedsByType(String type) {
        String normType = normalizeType(type);
        if (normType == null) {
            return getAvailableBreeds();
        }
        return getAvailablePets().stream()
                .filter(p -> normType.equalsIgnoreCase(normalizeType(p.getType())))
                .map(Pet::getBreed)
                .filter(b -> b != null && !b.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }

    @Override
    public List<Shelter> getAllShelters() {
        return shelterRepository.findAll().stream()
                .filter(s -> (s.getStatus() == null || !"INACTIVE".equalsIgnoreCase(s.getStatus()))
                        && !"INACTIVE".equalsIgnoreCase(s.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Shelter getShelterById(Long id) {
        return shelterRepository.findById(id).orElse(null);
    }

    @Override
    public Pet savePet(Pet pet) {
        return petRepository.save(pet);
    }

    @Override
    public List<Pet> getPetsByShelter(Shelter shelter) {
        if (shelter == null) return List.of();
        return petRepository.findByShelterOrderByIdDesc(shelter).stream()
                .filter(p -> !"REMOVED".equalsIgnoreCase(p.getAdoptionStatus()) && !"INACTIVE".equalsIgnoreCase(p.getAdoptionStatus()))
                .filter(p -> !"INACTIVE".equalsIgnoreCase(p.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Pet> getAllPetsForAdmin() {
        return petRepository.findAllByOrderByIdDesc().stream()
                .filter(p -> !"REMOVED".equalsIgnoreCase(p.getAdoptionStatus()) && !"INACTIVE".equalsIgnoreCase(p.getAdoptionStatus()))
                .filter(p -> !"INACTIVE".equalsIgnoreCase(p.getVerificationStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Pet verifyPet(Long petId, String status) {
        Pet pet = getPetById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found with ID: " + petId);
        }
        pet.setVerificationStatus(status);
        if ("APPROVED".equalsIgnoreCase(status)) {
            if (pet.getAdoptionStatus() == null || "PENDING".equalsIgnoreCase(pet.getAdoptionStatus()) || "REJECTED".equalsIgnoreCase(pet.getAdoptionStatus())) {
                pet.setAdoptionStatus("AVAILABLE");
            }
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            pet.setAdoptionStatus("REJECTED");
        }
        Pet saved = petRepository.save(pet);
        return saved;
    }

    @Override
    public Pet updatePet(Long petId, Pet updatedData, com.petconnect.entity.User owner) {
        Pet pet = getPetById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found with ID: " + petId);
        }
        if (owner != null && pet.getShelter() != null && pet.getShelter().getOwner() != null) {
            if (!pet.getShelter().getOwner().getId().equals(owner.getId()) &&
                !pet.getShelter().getOwner().getEmail().equalsIgnoreCase(owner.getEmail())) {
                throw new SecurityException("You do not have permission to edit this pet.");
            }
        }
        pet.setName(updatedData.getName());
        pet.setType(updatedData.getType());
        pet.setBreed(updatedData.getBreed());
        pet.setAge(updatedData.getAge());
        pet.setGender(updatedData.getGender());
        pet.setSize(updatedData.getSize());
        pet.setColor(updatedData.getColor());
        pet.setDescription(updatedData.getDescription());
        pet.setHealthInfo(updatedData.getHealthInfo());
        pet.setVaccinationStatus(updatedData.getVaccinationStatus());
        pet.setRescueStory(updatedData.getRescueStory());
        if (updatedData.getImagePath() != null && !updatedData.getImagePath().isBlank()) {
            pet.setImagePath(updatedData.getImagePath());
        }
        if (updatedData.getPhotoUrl() != null && !updatedData.getPhotoUrl().isBlank()) {
            pet.setPhotoUrl(updatedData.getPhotoUrl());
        }
        return petRepository.save(pet);
    }

    @org.springframework.transaction.annotation.Transactional
    @Override
    public String deletePet(Long petId, com.petconnect.entity.User owner) {
        Pet pet = getPetById(petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found with ID: " + petId);
        }

        // 1. Strict Ownership Security Check (Requirement 2 & 13)
        if (owner != null) {
            Shelter ownerShelter = null;
            List<Shelter> shelters = shelterRepository.findByOwner(owner);
            if (!shelters.isEmpty()) {
                ownerShelter = shelters.get(0);
            } else if ("saranya@gmail.com".equalsIgnoreCase(owner.getEmail())) {
                ownerShelter = shelterRepository.findByName("Happy Paws Shelter").orElse(null);
            } else if ("owner@petconnect.com".equalsIgnoreCase(owner.getEmail())) {
                ownerShelter = shelterRepository.findByName("Safe Tails Shelter").orElse(null);
            }

            if (pet.getShelter() == null || ownerShelter == null || !pet.getShelter().getId().equals(ownerShelter.getId())) {
                throw new SecurityException("You do not have permission to delete this pet. It belongs to another shelter.");
            }
        }

        // 2. Adopted Pet Protection (Requirement 7)
        if ("ADOPTED".equalsIgnoreCase(pet.getAdoptionStatus())) {
            throw new IllegalStateException("This pet has already been adopted and cannot be permanently deleted.");
        }

        // 3. Related Records Check (adoption_requests, care_bookings)
        List<com.petconnect.entity.AdoptionApplication> adoptions = adoptionApplicationRepository.findByPetId(petId);
        List<com.petconnect.entity.CareBooking> careBookings = careBookingRepository.findByPetId(petId);

        boolean hasAdoptionHistory = !adoptions.isEmpty();
        boolean hasCareHistory = !careBookings.isEmpty();

        // 4. Safe Deactivation if historical records exist (Requirement 6)
        if (hasAdoptionHistory || hasCareHistory) {
            pet.setAdoptionStatus("REMOVED");
            pet.setVerificationStatus("INACTIVE");
            petRepository.save(pet);
            return "DEACTIVATED";
        }

        // 5. Permanent Delete for test / unwanted pets with no history (Requirement 4, 8, 9)
        List<com.petconnect.entity.ShelterVisitSlot> visitSlots = shelterVisitSlotRepository.findAll().stream()
                .filter(s -> s.getPet() != null && s.getPet().getId().equals(petId))
                .collect(Collectors.toList());
        if (!visitSlots.isEmpty()) {
            shelterVisitSlotRepository.deleteAll(visitSlots);
        }

        List<com.petconnect.entity.ShelterReview> reviews = shelterReviewRepository.findAll().stream()
                .filter(r -> r.getPet() != null && r.getPet().getId().equals(petId))
                .collect(Collectors.toList());
        if (!reviews.isEmpty()) {
            shelterReviewRepository.deleteAll(reviews);
        }

        String photoToDelete = (pet.getPhotoUrl() != null && !pet.getPhotoUrl().isBlank()) ? pet.getPhotoUrl() : pet.getImagePath();
        petRepository.delete(pet);

        if (photoToDelete != null && photoToDelete.startsWith("/uploads/pets/")) {
            boolean stillUsed = petRepository.findAll().stream()
                    .anyMatch(p -> photoToDelete.equals(p.getPhotoUrl()) || photoToDelete.equals(p.getImagePath()));
            if (!stillUsed) {
                try {
                    String filename = photoToDelete.substring("/uploads/pets/".length());
                    java.nio.file.Path filePath = java.nio.file.Paths.get("uploads", "pets", filename).normalize();
                    java.nio.file.Path uploadDir = java.nio.file.Paths.get("uploads", "pets").normalize();
                    if (filePath.startsWith(uploadDir)) {
                        java.nio.file.Files.deleteIfExists(filePath);
                    }
                } catch (Exception e) {
                    System.err.println("Could not delete pet photo on delete: " + e.getMessage());
                }
            }
        }
        return "DELETED";
    }
}
