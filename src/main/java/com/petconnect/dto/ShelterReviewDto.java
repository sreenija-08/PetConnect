package com.petconnect.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ShelterReviewDto {

    private Long adoptionRequestId;

    @NotNull(message = "Please select a rating from 1 to 5 stars")
    @Min(value = 1, message = "Rating must be at least 1 star")
    @Max(value = 5, message = "Rating cannot exceed 5 stars")
    private Integer rating;

    @NotBlank(message = "Please share your experience with the shelter")
    private String comment;

    public ShelterReviewDto() {}

    public Long getAdoptionRequestId() { return adoptionRequestId; }
    public void setAdoptionRequestId(Long adoptionRequestId) { this.adoptionRequestId = adoptionRequestId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
