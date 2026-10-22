package com.ecoguard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class StatusUpdateRequestDto {

    @NotBlank(message = "New status is required")
    @Pattern(regexp = "(?i)^(SUBMITTED|UNDER_REVIEW|IN_PROGRESS|RESOLVED|REJECTED)$", 
             message = "Status must be one of: SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED, REJECTED")
    private String status; // SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED, REJECTED

    private String updatedBy;

    private String userRole; // ADMIN, VOLUNTEER, CITIZEN

    private String comment;

    public StatusUpdateRequestDto() {
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
