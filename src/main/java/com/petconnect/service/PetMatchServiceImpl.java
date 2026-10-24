package com.petconnect.service;

import com.petconnect.dto.PetMatchPreferenceDto;
import com.petconnect.dto.PetMatchResultDto;
import com.petconnect.entity.Pet;
import com.petconnect.entity.User;
import com.petconnect.entity.UserPetPreference;
import com.petconnect.repository.UserPetPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PetMatchServiceImpl implements PetMatchService {

    private static final Logger log = LoggerFactory.getLogger(PetMatchServiceImpl.class);

    private final PetService petService;
    private final FavoriteService favoriteService;
    private final UserPetPreferenceRepository preferenceRepository;

    @Autowired
    public PetMatchServiceImpl(PetService petService,
                               FavoriteService favoriteService,
                               UserPetPreferenceRepository preferenceRepository) {
        this.petService = petService;
        this.favoriteService = favoriteService;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public List<PetMatchResultDto> getRecommendations(User user, PetMatchPreferenceDto preferences) {
        if (preferences == null) {
            preferences = new PetMatchPreferenceDto();
        }

        // 1. Fetch only eligible pets (APPROVED + AVAILABLE from approved active shelters)
        List<Pet> eligiblePets = petService.getAvailablePets();
        log.info("Calculating PetMatch AI recommendations across {} eligible available pets for user {}",
                eligiblePets.size(), user != null ? user.getEmail() : "anonymous");

        // 2. Fetch user's existing favorites
        Set<Long> favoritePetIds = (user != null && user.getId() != null)
                ? favoriteService.getFavoritePetIdsForUser(user.getId())
                : Collections.emptySet();

        // 3. Score every eligible pet using transparent weighted scoring
        List<PetMatchResultDto> results = new ArrayList<>();
        for (Pet pet : eligiblePets) {
            boolean isFav = favoritePetIds.contains(pet.getId());
            PetMatchResultDto result = calculateCompatibility(preferences, pet, isFav);
            results.add(result);
        }

        // 4. Rank descending by compatibility score
        Collections.sort(results);

        return results;
    }

    @Override
    public PetMatchResultDto calculateCompatibility(PetMatchPreferenceDto preferences, Pet pet, boolean isFavorite) {
        if (preferences == null) preferences = new PetMatchPreferenceDto();
        if (pet == null) return new PetMatchResultDto(null, 0, false);

        double totalScore = 0.0;
        List<String> matchReasons = new ArrayList<>();
        List<String> concernNotes = new ArrayList<>();

        // ==========================================
        // 1. PET TYPE (Weight: 25%)
        // ==========================================
        String prefType = preferences.getPreferredPetType().trim().toUpperCase();
        String petType = (pet.getType() != null) ? pet.getType().trim().toUpperCase() : "";

        if ("ANY".equals(prefType)) {
            totalScore += 25.0;
            matchReasons.add("Matches your open pet type preference (" + formatCapital(pet.getType()) + ")");
        } else if (petType.startsWith(prefType) || prefType.startsWith(petType)) {
            totalScore += 25.0;
            matchReasons.add("Matches your preferred pet type (" + formatCapital(pet.getType()) + ")");
        } else {
            // Pet type mismatch (e.g. User wants Dog, but pet is Cat) -> 0 points
            totalScore += 0.0;
            concernNotes.add("Different pet type (" + formatCapital(pet.getType()) + " vs preferred " + formatCapital(preferences.getPreferredPetType()) + ")");
        }

        // ==========================================
        // 2. ACTIVITY LEVEL (Weight: 20%)
        // ==========================================
        String prefActivity = normalizeActivity(preferences.getActivityLevel());
        String petActivity = normalizeActivity(pet.getActivityLevel());

        if (petActivity == null) {
            // Documented rule: Missing activity data receives neutral partial score (8 / 20 = 40%), NOT full score!
            totalScore += 8.0;
            concernNotes.add("Activity level not specified by shelter (neutral score applied)");
        } else {
            int prefActVal = activityToNumber(prefActivity); // 1 = Low, 2 = Medium, 3 = High
            int petActVal = activityToNumber(petActivity);
            int diff = Math.abs(prefActVal - petActVal);

            if (diff == 0) {
                totalScore += 20.0;
                matchReasons.add("Activity level matches (" + formatCapital(pet.getActivityLevel()) + ")");
            } else if (diff == 1) {
                totalScore += 12.0;
                matchReasons.add("Close activity level match (" + formatCapital(pet.getActivityLevel()) + ")");
            } else {
                totalScore += 4.0;
                concernNotes.add("Energy level differs (" + formatCapital(pet.getActivityLevel()) + " vs preferred " + formatCapital(preferences.getActivityLevel()) + ")");
            }
        }

        // ==========================================
        // 3. HOME COMPATIBILITY (Weight: 15%)
        // ==========================================
        String home = preferences.getHomeType().trim().toLowerCase();
        String petSize = (pet.getSize() != null) ? pet.getSize().trim().toUpperCase() : "MEDIUM";
        boolean isCat = petType.contains("CAT");
        boolean isHighEnergy = "HIGH".equalsIgnoreCase(petActivity);

        if (home.contains("apartment")) {
            if (isCat || ("SMALL".equalsIgnoreCase(petSize) && !isHighEnergy)) {
                totalScore += 15.0;
                matchReasons.add("Well-suited for apartment living");
            } else if ("MEDIUM".equalsIgnoreCase(petSize) && !isHighEnergy) {
                totalScore += 13.0;
                matchReasons.add("Comfortable fit for apartment living");
            } else if ("LARGE".equalsIgnoreCase(petSize) || isHighEnergy) {
                totalScore += 6.0;
                concernNotes.add("High energy or large pet in apartment (requires extra daily outdoor exercise)");
            } else {
                totalScore += 10.0;
            }
        } else if (home.contains("house")) {
            totalScore += 15.0;
            matchReasons.add("Great fit for a house environment");
        } else if (home.contains("farm") || home.contains("large")) {
            if (!isCat && (isHighEnergy || "LARGE".equalsIgnoreCase(petSize))) {
                totalScore += 15.0;
                matchReasons.add("Thrives in spacious open property");
            } else {
                totalScore += 13.0;
                matchReasons.add("Suitable for large property");
            }
        } else {
            totalScore += 11.0;
            matchReasons.add("Adaptable to your living arrangement");
        }

        // ==========================================
        // 4. PET EXPERIENCE (Weight: 15%)
        // ==========================================
        String userExp = preferences.getExperienceLevel().trim().toLowerCase();
        String petExp = (pet.getExperienceLevel() != null) ? pet.getExperienceLevel().trim().toLowerCase() : null;

        if (petExp == null) {
            // Missing data: neutral score (6 / 15 = 40%)
            totalScore += 6.0;
        } else if (userExp.contains("first") || userExp.contains("beginner")) {
            if (petExp.contains("beginner")) {
                totalScore += 15.0;
                matchReasons.add("Beginner-friendly companion, ideal for first-time owners");
            } else if (petExp.contains("some")) {
                totalScore += 7.0;
                concernNotes.add("May require basic prior pet training experience");
            } else {
                totalScore += 2.0;
                concernNotes.add("Requires an experienced handler with specialized training skills");
            }
        } else if (userExp.contains("some")) {
            if (petExp.contains("beginner")) {
                totalScore += 14.0;
                matchReasons.add("Easygoing pet well within your comfort zone");
            } else if (petExp.contains("some")) {
                totalScore += 15.0;
                matchReasons.add("Great match for your pet experience level");
            } else {
                totalScore += 8.0;
                concernNotes.add("Recommended for owners with advanced training background");
            }
        } else { // Experienced
            totalScore += 15.0;
            matchReasons.add("Matches your experienced pet owner background");
        }

        // ==========================================
        // 5. PET SIZE (Weight: 10%)
        // ==========================================
        String prefSize = preferences.getPreferredSize().trim().toUpperCase();
        String currentPetSize = (pet.getSize() != null) ? pet.getSize().trim().toUpperCase() : null;

        if ("ANY".equals(prefSize)) {
            totalScore += 10.0;
            matchReasons.add("Fits your flexible size preference");
        } else if (currentPetSize == null) {
            totalScore += 4.0;
        } else {
            int prefSizeVal = sizeToNumber(prefSize);
            int petSizeVal = sizeToNumber(currentPetSize);
            int diff = Math.abs(prefSizeVal - petSizeVal);

            if (diff == 0) {
                totalScore += 10.0;
                matchReasons.add("Size matches your preference (" + formatCapital(pet.getSize()) + ")");
            } else if (diff == 1) {
                totalScore += 6.0;
                matchReasons.add("Close to preferred size (" + formatCapital(pet.getSize()) + ")");
            } else {
                totalScore += 2.0;
                concernNotes.add("Size differs from preference (" + formatCapital(pet.getSize()) + " vs preferred " + formatCapital(preferences.getPreferredSize()) + ")");
            }
        }

        // ==========================================
        // 6. AGE PREFERENCE (Weight: 10%)
        // ==========================================
        String prefAge = preferences.getPreferredAge().trim().toUpperCase();
        String petAgeCat = (pet.getAgeCategory() != null) ? pet.getAgeCategory().trim().toUpperCase() : "UNKNOWN";

        if ("ANY".equals(prefAge)) {
            totalScore += 10.0;
            matchReasons.add("Fits your open age preference");
        } else if ("UNKNOWN".equals(petAgeCat)) {
            totalScore += 4.0;
        } else {
            int prefAgeVal = ageCategoryToNumber(prefAge);
            int petAgeVal = ageCategoryToNumber(petAgeCat);
            int diff = Math.abs(prefAgeVal - petAgeVal);

            if (diff == 0) {
                totalScore += 10.0;
                matchReasons.add("Age group matches (" + formatCapital(pet.getAgeCategory()) + ")");
            } else if (diff == 1) {
                totalScore += 6.0;
                matchReasons.add("Close to preferred age (" + formatCapital(pet.getAgeCategory()) + ")");
            } else {
                totalScore += 2.0;
                concernNotes.add("Age differs from preferred (" + formatCapital(pet.getAgeCategory()) + " vs " + formatCapital(preferences.getPreferredAge()) + ")");
            }
        }

        // ==========================================
        // 7. LOCATION (Weight: 5%)
        // ==========================================
        String userLoc = (preferences.getPreferredLocation() != null) ? preferences.getPreferredLocation().trim().toLowerCase() : "";
        String shelterLoc = "";
        if (pet.getShelter() != null) {
            String city = pet.getShelter().getCity() != null ? pet.getShelter().getCity() : "";
            String location = pet.getShelter().getLocation() != null ? pet.getShelter().getLocation() : "";
            shelterLoc = (city + " " + location).trim().toLowerCase();
        }

        if (userLoc.isBlank()) {
            totalScore += 5.0;
        } else if (!shelterLoc.isBlank() && shelterLoc.contains(userLoc)) {
            totalScore += 5.0;
            matchReasons.add("Located nearby in your preferred area (" + formatCapital(pet.getShelter().getCity() != null ? pet.getShelter().getCity() : pet.getShelter().getLocation()) + ")");
        } else {
            totalScore += 2.0;
        }

        // ==========================================
        // 8. CHILDREN AT HOME SAFETY ADJUSTMENT
        // ==========================================
        if (Boolean.TRUE.equals(preferences.getHasChildren())) {
            if (pet.getGoodWithChildren() == null) {
                // Not evaluated -> neutral, no penalty
            } else if (Boolean.TRUE.equals(pet.getGoodWithChildren())) {
                matchReasons.add("Verified friendly with children");
            } else {
                // Not suitable with children -> safety penalty (-20 points)
                totalScore -= 20.0;
                concernNotes.add("⚠️ Shelter advises this pet may not be suitable for homes with young children");
            }
        }

        // ==========================================
        // 9. TIME AVAILABLE ADJUSTMENT
        // ==========================================
        String timeAvail = preferences.getTimeAvailable().trim().toLowerCase();
        if (timeAvail.contains("less than 1") && "HIGH".equalsIgnoreCase(petActivity)) {
            totalScore -= 10.0;
            concernNotes.add("⚠️ High energy pet may require more time than your available 1 hour");
        } else if (timeAvail.contains("2–4") || timeAvail.contains("more than 4")) {
            matchReasons.add("Your daily available time supports this pet's exercise needs");
        }

        // Clamp final score strictly between 0 and 100
        int finalScore = Math.max(0, Math.min(100, (int) Math.round(totalScore)));

        PetMatchResultDto result = new PetMatchResultDto(pet, finalScore, isFavorite);
        result.setMatchReasons(matchReasons);
        result.setConcernNotes(concernNotes);

        return result;
    }

    @Override
    public Optional<UserPetPreference> getUserPreferences(User user) {
        if (user == null) return Optional.empty();
        return preferenceRepository.findByUser(user);
    }

    @Override
    @Transactional
    public UserPetPreference saveUserPreferences(User user, PetMatchPreferenceDto preferences) {
        if (user == null || preferences == null) return null;

        UserPetPreference pref = preferenceRepository.findByUser(user)
                .orElse(new UserPetPreference(user));

        pref.setPreferredPetType(preferences.getPreferredPetType());
        pref.setActivityLevel(preferences.getActivityLevel());
        pref.setHomeType(preferences.getHomeType());
        pref.setExperienceLevel(preferences.getExperienceLevel());
        pref.setTimeAvailable(preferences.getTimeAvailable());
        pref.setHasChildren(preferences.getHasChildren());
        pref.setPreferredSize(preferences.getPreferredSize());
        pref.setPreferredAge(preferences.getPreferredAge());
        pref.setPreferredLocation(preferences.getPreferredLocation());
        pref.setUpdatedAt(LocalDateTime.now());

        log.info("Saved PetMatch AI lifestyle preferences for user: {}", user.getEmail());
        return preferenceRepository.save(pref);
    }

    private String normalizeActivity(String act) {
        if (act == null || act.isBlank()) return null;
        String s = act.trim().toUpperCase();
        if (s.contains("LOW")) return "LOW";
        if (s.contains("MED")) return "MEDIUM";
        if (s.contains("HIGH")) return "HIGH";
        return null;
    }

    private int activityToNumber(String act) {
        if ("LOW".equalsIgnoreCase(act)) return 1;
        if ("HIGH".equalsIgnoreCase(act)) return 3;
        return 2; // Medium default
    }

    private int sizeToNumber(String size) {
        if (size == null) return 2;
        String s = size.trim().toUpperCase();
        if (s.contains("SMALL")) return 1;
        if (s.contains("LARGE")) return 3;
        return 2; // Medium default
    }

    private int ageCategoryToNumber(String ageCat) {
        if (ageCat == null) return 2;
        String a = ageCat.trim().toUpperCase();
        if (a.contains("YOUNG")) return 1;
        if (a.contains("SENIOR")) return 3;
        return 2; // Adult default
    }

    private String formatCapital(String s) {
        if (s == null || s.isBlank()) return "";
        s = s.trim();
        return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
    }
}
