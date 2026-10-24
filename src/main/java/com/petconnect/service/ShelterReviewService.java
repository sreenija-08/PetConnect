package com.petconnect.service;

import com.petconnect.dto.ShelterReviewDto;
import com.petconnect.entity.ShelterReview;
import com.petconnect.entity.User;

import java.util.List;
import java.util.Optional;

public interface ShelterReviewService {

    ShelterReview submitReview(User user, Long adoptionRequestId, ShelterReviewDto dto);

    Optional<ShelterReview> getReviewForApplication(Long adoptionRequestId);

    boolean hasReviewedApplication(Long adoptionRequestId);

    List<ShelterReview> getReviewsByUser(User user);

    List<ShelterReview> getReviewsByShelter(Long shelterId);

    List<ShelterReview> getAllReviews();
}
