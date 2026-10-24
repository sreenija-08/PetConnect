package com.petconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "shelter_reviews")
public class ShelterReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shelter_id", nullable = false)
    private Shelter shelter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "adoption_request_id", nullable = false, unique = true)
    private AdoptionApplication adoptionApplication;

    @Column(nullable = false)
    private int rating; // 1 to 5

    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShelterReview() {}

    public ShelterReview(User user, Shelter shelter, Pet pet, AdoptionApplication adoptionApplication, int rating, String comment) {
        this.user = user;
        this.shelter = shelter;
        this.pet = pet;
        this.adoptionApplication = adoptionApplication;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Shelter getShelter() { return shelter; }
    public void setShelter(Shelter shelter) { this.shelter = shelter; }

    public Pet getPet() { return pet; }
    public void setPet(Pet pet) { this.pet = pet; }

    public AdoptionApplication getAdoptionApplication() { return adoptionApplication; }
    public void setAdoptionApplication(AdoptionApplication adoptionApplication) { this.adoptionApplication = adoptionApplication; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStarsDisplay() {
        int r = Math.max(1, Math.min(5, rating));
        return "★".repeat(r) + "☆".repeat(5 - r);
    }
}
