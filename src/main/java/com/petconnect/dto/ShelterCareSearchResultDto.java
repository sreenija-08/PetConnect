package com.petconnect.dto;

import com.petconnect.entity.Shelter;
import java.time.LocalDate;

public class ShelterCareSearchResultDto {

    private Shelter shelter;
    private int totalCapacity;
    private int availableCapacity;
    private double boardingFeePerDay;
    private double estimatedTotalFee;
    private int numberOfDays;
    private boolean hasCapacity;
    private LocalDate startDate;
    private LocalDate endDate;
    private String petType;

    public ShelterCareSearchResultDto() {}

    public ShelterCareSearchResultDto(Shelter shelter, int totalCapacity, int availableCapacity,
                                      double boardingFeePerDay, double estimatedTotalFee, int numberOfDays,
                                      boolean hasCapacity, LocalDate startDate, LocalDate endDate, String petType) {
        this.shelter = shelter;
        this.totalCapacity = totalCapacity;
        this.availableCapacity = availableCapacity;
        this.boardingFeePerDay = boardingFeePerDay;
        this.estimatedTotalFee = estimatedTotalFee;
        this.numberOfDays = numberOfDays;
        this.hasCapacity = hasCapacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.petType = petType;
    }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(int availableCapacity) { this.availableCapacity = availableCapacity; }

    public double getBoardingFeePerDay() { return boardingFeePerDay; }
    public void setBoardingFeePerDay(double boardingFeePerDay) { this.boardingFeePerDay = boardingFeePerDay; }

    public double getEstimatedTotalFee() { return estimatedTotalFee; }
    public void setEstimatedTotalFee(double estimatedTotalFee) { this.estimatedTotalFee = estimatedTotalFee; }

    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) { this.numberOfDays = numberOfDays; }

    public boolean isHasCapacity() { return hasCapacity; }
    public void setHasCapacity(boolean hasCapacity) { this.hasCapacity = hasCapacity; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getPetType() { return petType; }
    public void setPetType(String petType) { this.petType = petType; }

    public int getAvailableSlots() { return availableCapacity; }
    public boolean isHasAvailability() { return hasCapacity; }
}
