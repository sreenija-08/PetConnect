package com.petconnect.dto;

public class ShelterDto {
    private Long id;
    private String name;
    private String location;
    private String description;
    private Integer capacity;
    private Integer availableCapacity;
    private String verificationStatus;
    private Boolean temporaryCareEnabled;
    private String ownerName;
    private String phone;
    private String email;

    public ShelterDto() {}

    public ShelterDto(Long id, String name, String location, String description, Integer capacity, 
                      Integer availableCapacity, String verificationStatus, Boolean temporaryCareEnabled, 
                      String ownerName, String phone, String email) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.description = description;
        this.capacity = capacity;
        this.availableCapacity = availableCapacity;
        this.verificationStatus = verificationStatus;
        this.temporaryCareEnabled = temporaryCareEnabled;
        this.ownerName = ownerName;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(Integer availableCapacity) { this.availableCapacity = availableCapacity; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public Boolean getTemporaryCareEnabled() { return temporaryCareEnabled; }
    public void setTemporaryCareEnabled(Boolean temporaryCareEnabled) { this.temporaryCareEnabled = temporaryCareEnabled; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
