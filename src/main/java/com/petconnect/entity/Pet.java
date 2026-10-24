package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String type; // DOG, CAT

    private String breed;

    private Integer age;

    private String gender;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imagePath;

    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    private String healthInfo;

    private String size; // Small, Medium, Large

    private String color;

    private String vaccinationStatus; // Vaccinated, Partially Vaccinated, Not Vaccinated

    @Column(columnDefinition = "TEXT")
    private String rescueStory;

    // Recommendation Attributes
    private String activityLevel; // Low, Medium, High

    private Boolean goodWithChildren; // true, false, null (unspecified)

    private String experienceLevel; // Beginner-friendly, Some Experience, Experienced

    private String ageCategory; // Young, Adult, Senior

    private String verificationStatus = "APPROVED";

    private String adoptionStatus = "AVAILABLE"; // AVAILABLE, ADOPTED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    private String ownerName;

    @Column(columnDefinition = "TEXT")
    private String specialCareInstructions;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public Pet() {}

    public Pet(String name, String type, String breed, Integer age, String gender, 
               String description, String imagePath, String healthInfo, 
               String verificationStatus, String adoptionStatus, Shelter shelter, String ownerName) {
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.gender = gender;
        this.description = description;
        this.imagePath = imagePath;
        this.healthInfo = healthInfo;
        this.verificationStatus = verificationStatus;
        this.adoptionStatus = adoptionStatus;
        this.shelter = shelter;
        this.ownerName = ownerName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getHealthInfo() { return healthInfo; }
    public void setHealthInfo(String healthInfo) { this.healthInfo = healthInfo; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getVaccinationStatus() { return vaccinationStatus; }
    public void setVaccinationStatus(String vaccinationStatus) { this.vaccinationStatus = vaccinationStatus; }

    public String getRescueStory() { return rescueStory; }
    public void setRescueStory(String rescueStory) { this.rescueStory = rescueStory; }

    public String getActivityLevel() { return activityLevel; }
    public void setActivityLevel(String activityLevel) { this.activityLevel = activityLevel; }

    public Boolean getGoodWithChildren() { return goodWithChildren; }
    public void setGoodWithChildren(Boolean goodWithChildren) { this.goodWithChildren = goodWithChildren; }
    public boolean isGoodWithChildren() { return Boolean.TRUE.equals(goodWithChildren); }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getAgeCategory() {
        if (ageCategory != null && !ageCategory.isBlank()) {
            return ageCategory;
        }
        if (age != null) {
            if (age <= 2) return "Young";
            if (age <= 7) return "Adult";
            return "Senior";
        }
        return "Unknown";
    }
    public void setAgeCategory(String ageCategory) { this.ageCategory = ageCategory; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getAdoptionStatus() { return adoptionStatus; }
    public void setAdoptionStatus(String adoptionStatus) { this.adoptionStatus = adoptionStatus; }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getDisplayPhoto() {
        if (photoUrl != null && !photoUrl.isBlank()) {
            return photoUrl;
        }
        if (imagePath != null && !imagePath.isBlank()) {
            return imagePath;
        }
        return "DOG".equalsIgnoreCase(type) ? 
            "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&q=80&w=800" :
            "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&q=80&w=800";
    }

    public String getSpecialCareInstructions() { return specialCareInstructions; }
    public void setSpecialCareInstructions(String specialCareInstructions) { this.specialCareInstructions = specialCareInstructions; }

    public boolean isUserOwnedPet() {
        return user != null || "OWNED".equalsIgnoreCase(adoptionStatus);
    }

    public String getShelterName() {
        return shelter != null ? shelter.getName() : "Local Shelter";
    }

    public boolean isVaccinated() {
        return vaccinationStatus != null && 
               vaccinationStatus.toLowerCase().contains("vaccinat") && 
               !vaccinationStatus.toLowerCase().contains("not");
    }

    public Boolean getVaccinated() {
        return isVaccinated();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return adoptionStatus != null ? adoptionStatus : "AVAILABLE";
    }

    public void setStatus(String status) {
        this.adoptionStatus = status;
    }

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();

    public List<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<Favorite> favorites) {
        this.favorites = favorites;
    }
}
