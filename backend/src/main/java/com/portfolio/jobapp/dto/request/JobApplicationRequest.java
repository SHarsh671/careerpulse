package com.portfolio.jobapp.dto.request;

import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class JobApplicationRequest {

    @NotNull(message = "Company is required")
    private Long companyId;

    @NotBlank(message = "Job title is required")
    @Size(max = 150, message = "Job title must not exceed 150 characters")
    private String jobTitle;

    @Size(max = 150, message = "Location must not exceed 150 characters")
    private String location;

    @Size(max = 500, message = "Job URL must not exceed 500 characters")
    private String jobUrl;

    private ApplicationStatus status = ApplicationStatus.SAVED;

    @PositiveOrZero(message = "Minimum salary cannot be negative")
    private Integer salaryMin;

    @PositiveOrZero(message = "Maximum salary cannot be negative")
    private Integer salaryMax;

    private LocalDate appliedDate;

    private LocalDate deadline;

    private String notes;

    public JobApplicationRequest() {
    }

    public JobApplicationRequest(Long companyId, String jobTitle, String location, String jobUrl,
                                 ApplicationStatus status, Integer salaryMin, Integer salaryMax,
                                 LocalDate appliedDate, LocalDate deadline, String notes) {
        this.companyId = companyId;
        this.jobTitle = jobTitle;
        this.location = location;
        this.jobUrl = jobUrl;
        this.status = status;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.appliedDate = appliedDate;
        this.deadline = deadline;
        this.notes = notes;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Integer getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Integer salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Integer getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Integer salaryMax) {
        this.salaryMax = salaryMax;
    }

    public LocalDate getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(LocalDate appliedDate) {
        this.appliedDate = appliedDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

