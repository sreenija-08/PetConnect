package com.petconnect.dto;

public class CareBookingDto {
    private Long id;
    private String petName;
    private String petType;
    private String petBreed;
    private String ownerName;
    private String shelterName;
    private String startDate;
    private String endDate;
    private String status;
    private String notes;
    private String requestedDate;

    public CareBookingDto() {}

    public CareBookingDto(Long id, String petName, String petType, String petBreed, String ownerName, 
                          String shelterName, String startDate, String endDate, String status, String notes, String requestedDate) {
        this.id = id;
        this.petName = petName;
        this.petType = petType;
        this.petBreed = petBreed;
        this.ownerName = ownerName;
        this.shelterName = shelterName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.notes = notes;
        this.requestedDate = requestedDate;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }

    public String getPetBreed() { return petBreed; }
    public void setPetBreed(String petBreed) { this.petBreed = petBreed; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getShelterName() { return shelterName; }
    public void setShelterName(String shelterName) { this.shelterName = shelterName; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getRequestedDate() { return requestedDate; }
    public void setRequestedDate(String requestedDate) { this.requestedDate = requestedDate; }
}
