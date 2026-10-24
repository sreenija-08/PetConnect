package com.petconnect.dto;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class CareBookingRequestDto {

    private Long petId;
    private Long shelterId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private String emergencyContact;
    private String specialInstructions;
    private String medicalInformation;
    private String vaccinationInfo;
    private String additionalNotes;

    public CareBookingRequestDto() {}

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public Long getShelterId() { return shelterId; }
    public void setShelterId(Long shelterId) { this.shelterId = shelterId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getEmergencyContact() { return emergencyContact; }
    public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }

    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }

    public String getMedicalInformation() { return medicalInformation; }
    public void setMedicalInformation(String medicalInformation) { this.medicalInformation = medicalInformation; }

    public String getVaccinationInfo() { return vaccinationInfo; }
    public void setVaccinationInfo(String vaccinationInfo) { this.vaccinationInfo = vaccinationInfo; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}
