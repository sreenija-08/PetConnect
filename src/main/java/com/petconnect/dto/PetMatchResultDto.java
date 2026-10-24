package com.petconnect.dto;

import com.petconnect.entity.Pet;
import java.util.ArrayList;
import java.util.List;

public class PetMatchResultDto implements Comparable<PetMatchResultDto> {

    private Pet pet;
    private int compatibilityScore;
    private String compatibilityBadge; // "High Compatibility", "Potential Match", "Lower Match"
    private List<String> matchReasons = new ArrayList<>();
    private List<String> concernNotes = new ArrayList<>();
    private boolean favorite;

    public PetMatchResultDto() {}

    public PetMatchResultDto(Pet pet, int compatibilityScore, boolean favorite) {
        this.pet = pet;
        this.compatibilityScore = compatibilityScore;
        this.favorite = favorite;
        this.compatibilityBadge = determineBadge(compatibilityScore);
    }

    public static String determineBadge(int score) {
        if (score >= 80) return "High Compatibility";
        if (score >= 60) return "Potential Match";
        return "Lower Match";
    }

    public String getBadgeCssClass() {
        if (compatibilityScore >= 80) return "bg-success text-white";
        if (compatibilityScore >= 60) return "bg-purple text-white";
        return "bg-secondary text-white";
    }

    public String getScoreColor() {
        if (compatibilityScore >= 80) return "text-success";
        if (compatibilityScore >= 60) return "text-purple";
        return "text-muted";
    }

    @Override
    public int compareTo(PetMatchResultDto other) {
        if (other == null) return -1;
        // Higher score first
        int cmp = Integer.compare(other.compatibilityScore, this.compatibilityScore);
        if (cmp != 0) return cmp;
        if (this.pet != null && other.pet != null && this.pet.getId() != null && other.pet.getId() != null) {
            return Long.compare(other.pet.getId(), this.pet.getId());
        }
        return 0;
    }

    // Getters and Setters
    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public int getCompatibilityScore() {
        return compatibilityScore;
    }

    public void setCompatibilityScore(int compatibilityScore) {
        this.compatibilityScore = compatibilityScore;
        this.compatibilityBadge = determineBadge(compatibilityScore);
    }

    public String getCompatibilityBadge() {
        return compatibilityBadge != null ? compatibilityBadge : determineBadge(compatibilityScore);
    }

    public void setCompatibilityBadge(String compatibilityBadge) {
        this.compatibilityBadge = compatibilityBadge;
    }

    public List<String> getMatchReasons() {
        return matchReasons;
    }

    public void setMatchReasons(List<String> matchReasons) {
        this.matchReasons = matchReasons;
    }

    public void addMatchReason(String reason) {
        if (reason != null && !reason.isBlank()) {
            this.matchReasons.add(reason);
        }
    }

    public List<String> getConcernNotes() {
        return concernNotes;
    }

    public void setConcernNotes(List<String> concernNotes) {
        this.concernNotes = concernNotes;
    }

    public void addConcernNote(String note) {
        if (note != null && !note.isBlank()) {
            this.concernNotes.add(note);
        }
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
