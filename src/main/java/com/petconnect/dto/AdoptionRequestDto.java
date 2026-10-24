package com.petconnect.dto;

public class AdoptionRequestDto {
    private Long id;
    private String adopterName;
    private String petName;
    private String petBreed;
    private String petType;
    private String shelterName;
    private String requestedDate;
    private String status;
    private String message;
    private String remarks;

    public AdoptionRequestDto() {}

    public AdoptionRequestDto(Long id, String adopterName, String petName, String petBreed, String petType, 
                              String shelterName, String requestedDate, String status, String message, String remarks) {
        this.id = id;
        this.adopterName = adopterName;
        this.petName = petName;
        this.petBreed = petBreed;
        this.petType = petType;
        this.shelterName = shelterName;
        this.requestedDate = requestedDate;
        this.status = status;
        this.message = message;
        this.remarks = remarks;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAdopterName() { return adopterName; }
    public void setAdopterName(String adopterName) { this.adopterName = adopterName; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }

    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getRequestedDate() { return requestedDate; }
    public void setRequestedDate(String requestedDate) { this.requestedDate = requestedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}
