package com.petconnect.service;

import com.petconnect.dto.ShelterReviewDto;
import com.petconnect.entity.*;
import com.petconnect.repository.AdoptionApplicationRepository;
import com.petconnect.repository.ShelterReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShelterReviewServiceImpl implements ShelterReviewService {

    private final ShelterReviewRepository reviewRepo;
    private final AdoptionApplicationRepository adoptionRepo;
    private final NotificationService notificationService;

    @Autowired
    public ShelterReviewServiceImpl(ShelterReviewRepository reviewRepo,
                                   AdoptionApplicationRepository adoptionRepo,
                                   NotificationService notificationService) {
        this.reviewRepo = reviewRepo;
        this.adoptionRepo = adoptionRepo;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public ShelterReview submitReview(User user, Long adoptionRequestId, ShelterReviewDto dto) {
        AdoptionApplication app = adoptionRepo.findById(adoptionRequestId)
                .orElseThrow(() -> new IllegalArgumentException("Adoption request not found: " + adoptionRequestId));

        // 1. Validate ownership: user must be the adopter
        if (!app.getUser().getId().equals(user.getId())) {
            throw new SecurityException("You can only review shelters for your own adoption requests.");
        }

        // 2. Validate adoption status: MUST be COMPLETED
        if (app.getStatus() != AdoptionStatus.COMPLETED) {
            throw new IllegalStateException("You can only review the shelter after the adoption has been successfully completed.");
        }

        // 3. Prevent Shelter Owner from reviewing their own shelter
        if (app.getShelter() != null && app.getShelter().getOwner() != null &&
            app.getShelter().getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("Shelter owners cannot review their own shelter.");
        }

        // 4. One review per completed adoption
        if (reviewRepo.existsByAdoptionApplicationId(adoptionRequestId)) {
            throw new IllegalStateException("You have already submitted a review for this adoption.");
        }

        // 5. Create and save review
        ShelterReview review = new ShelterReview();
        review.setUser(user);
        review.setShelter(app.getShelter());
        review.setPet(app.getPet());
        review.setAdoptionApplication(app);
        review.setRating(Math.max(1, Math.min(5, dto.getRating())));
        review.setComment(dto.getComment().trim());
        review.setCreatedAt(LocalDateTime.now());

        ShelterReview saved = reviewRepo.save(review);

        // Notify shelter owner
        if (app.getShelter() != null && app.getShelter().getOwner() != null) {
            notificationService.createNotification(
                    app.getShelter().getOwner(),
                    "New Shelter Review (" + saved.getRating() + "★)",
                    user.getFullName() + " submitted a " + saved.getRating() + "-star review for " +
                    app.getShelter().getName() + " regarding the adoption of " + app.getPet().getName() + ".",
                    "REVIEW"
            );
        }

        return saved;
    }

    @Override
    public Optional<ShelterReview> getReviewForApplication(Long adoptionRequestId) {
        return reviewRepo.findByAdoptionApplicationId(adoptionRequestId);
    }

    @Override
    public boolean hasReviewedApplication(Long adoptionRequestId) {
        return reviewRepo.existsByAdoptionApplicationId(adoptionRequestId);
    }

    @Override
    public List<ShelterReview> getReviewsByUser(User user) {
        return reviewRepo.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public List<ShelterReview> getReviewsByShelter(Long shelterId) {
        return reviewRepo.findByShelterIdOrderByCreatedAtDesc(shelterId);
    }

    @Override
    public List<ShelterReview> getAllReviews() {
        return reviewRepo.findAllByOrderByCreatedAtDesc();
    }
}
