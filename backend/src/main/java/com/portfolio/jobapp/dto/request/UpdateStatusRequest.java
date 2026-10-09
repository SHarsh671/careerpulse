package com.portfolio.jobapp.dto.request;

import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    public UpdateStatusRequest() {
    }

    public UpdateStatusRequest(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}

