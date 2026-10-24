package com.petconnect.dto;

public class PetDto {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private Integer age;
    private String gender;
    private String description;
    private String imagePath;
    private String healthInfo;
    private String verificationStatus;
    private String adoptionStatus;
    private String shelterName;
    private String ownerName;

    public PetDto() {}

    public PetDto(Long id, String name, String type, String breed, Integer age, String gender, 
                  String description, String imagePath, String healthInfo, 
                  String verificationStatus, String adoptionStatus, String shelterName, String ownerName) {
        this.id = id;
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
        this.shelterName = shelterName;
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

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getAdoptionStatus() { return adoptionStatus; }
    public void setAdoptionStatus(String adoptionStatus) { this.adoptionStatus = adoptionStatus; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
