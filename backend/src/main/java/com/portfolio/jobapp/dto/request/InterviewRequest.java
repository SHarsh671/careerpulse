package com.portfolio.jobapp.dto.request;

import com.portfolio.jobapp.entity.enums.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class InterviewRequest {

    @NotNull(message = "Interview type is required")
    private InterviewType type;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    @Size(max = 150, message = "Interviewer name must not exceed 150 characters")
    private String interviewer;

    private String notes;

    @Size(max = 50, message = "Result must not exceed 50 characters")
    private String result;

    public InterviewRequest() {
    }

    public InterviewRequest(InterviewType type, LocalDateTime scheduledAt, String interviewer, String notes, String result) {
        this.type = type;
        this.scheduledAt = scheduledAt;
        this.interviewer = interviewer;
        this.notes = notes;
        this.result = result;
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
}

