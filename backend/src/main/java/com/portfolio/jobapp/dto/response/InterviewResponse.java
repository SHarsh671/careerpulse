package com.portfolio.jobapp.dto.response;

import com.portfolio.jobapp.entity.enums.InterviewType;
import java.time.LocalDateTime;

public class InterviewResponse {

    private Long id;
    private Long applicationId;
    private InterviewType type;
    private LocalDateTime scheduledAt;
    private String interviewer;
    private String notes;
    private String result;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public InterviewResponse() {
    }

    public InterviewResponse(Long id, Long applicationId, InterviewType type, LocalDateTime scheduledAt,
                             String interviewer, String notes, String result, LocalDateTime createdAt,
                             LocalDateTime updatedAt) {
        this.id = id;
        this.applicationId = applicationId;
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.interviewer = interviewer;
        this.notes = notes;
        this.result = result;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public InterviewType getType() {
        return type;
    }

    public void setType(InterviewType type) {
        this.type = type;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getInterviewer() {
        return interviewer;
    }

    public void setInterviewer(String interviewer) {
        this.interviewer = interviewer;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

