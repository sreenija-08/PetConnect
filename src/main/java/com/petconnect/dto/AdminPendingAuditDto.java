package com.petconnect.dto;

import java.time.LocalDateTime;

public class AdminPendingAuditDto implements Comparable<AdminPendingAuditDto> {
    private Long id;
    private String type; // "SHELTER", "PET", "ADOPTION"
    private String typeDisplay; // "Shelter Verification", "Pet Verification", "Adoption Review"
    private String typeIcon; // "🏠", "🐶", "📄"
    private String subjectTitle;
    private String subjectSubtitle;
    private String subjectDetails;
    private LocalDateTime submittedDate;
    private String status; // "PENDING", "PENDING ADMIN REVIEW"
    private String statusBadgeClass; // "bg-warning text-dark", "bg-info text-dark"
    private String reviewUrl;

    public AdminPendingAuditDto() {}

    public AdminPendingAuditDto(Long id, String type, String typeDisplay, String typeIcon,
                                String subjectTitle, String subjectSubtitle,
                                LocalDateTime submittedDate, String status, String statusBadgeClass,
                                String reviewUrl) {
        this.id = id;
        this.type = type;
        this.typeDisplay = typeDisplay;
        this.typeIcon = typeIcon;
        this.subjectTitle = subjectTitle;
        this.subjectSubtitle = subjectSubtitle;
        if (subjectSubtitle != null && !subjectSubtitle.isBlank()) {
            this.subjectDetails = subjectTitle + " (" + subjectSubtitle + ")";
        } else {
            this.subjectDetails = subjectTitle;
        }
        this.submittedDate = submittedDate;
        this.status = status;
        this.statusBadgeClass = statusBadgeClass;
        this.reviewUrl = reviewUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeDisplay() {
        return typeDisplay;
    }

    public void setTypeDisplay(String typeDisplay) {
        this.typeDisplay = typeDisplay;
    }

    public String getTypeIcon() {
        return typeIcon;
    }

    public void setTypeIcon(String typeIcon) {
        this.typeIcon = typeIcon;
    }

    public String getSubjectTitle() {
        return subjectTitle;
    }

    public void setSubjectTitle(String subjectTitle) {
        this.subjectTitle = subjectTitle;
    }

    public String getSubjectSubtitle() {
        return subjectSubtitle;
    }

    public void setSubjectSubtitle(String subjectSubtitle) {
        this.subjectSubtitle = subjectSubtitle;
    }

    public String getSubjectDetails() {
        return subjectDetails;
    }

    public void setSubjectDetails(String subjectDetails) {
        this.subjectDetails = subjectDetails;
    }

    public LocalDateTime getSubmittedDate() {
        return submittedDate;
    }

    public void setSubmittedDate(LocalDateTime submittedDate) {
        this.submittedDate = submittedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusBadgeClass() {
        return statusBadgeClass;
    }

    public void setStatusBadgeClass(String statusBadgeClass) {
        this.statusBadgeClass = statusBadgeClass;
    }

    public String getReviewUrl() {
        return reviewUrl;
    }

    public void setReviewUrl(String reviewUrl) {
        this.reviewUrl = reviewUrl;
    }

    @Override
    public int compareTo(AdminPendingAuditDto other) {
        if (this.submittedDate == null && other.submittedDate == null) return 0;
        if (this.submittedDate == null) return 1;
        if (other.submittedDate == null) return -1;
        return other.submittedDate.compareTo(this.submittedDate); // latest first
    }
}
