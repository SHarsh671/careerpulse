package com.portfolio.jobapp.controller;

import com.portfolio.jobapp.dto.request.JobApplicationRequest;
import com.portfolio.jobapp.dto.request.UpdateStatusRequest;
import com.portfolio.jobapp.dto.response.JobApplicationResponse;
import com.portfolio.jobapp.dto.response.PagedResponse;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
public class JobApplicationController {

    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> createApplication(
            Authentication authentication,
            @Valid @RequestBody JobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.createApplication(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<JobApplicationResponse>> getApplications(
            Authentication authentication,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "appliedDate,desc") String sort) {

        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), sortObj);

        PagedResponse<JobApplicationResponse> response = jobApplicationService.getApplications(
                authentication.getName(), status, companyId, location, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getApplicationById(
            Authentication authentication,
            @PathVariable Long id) {
        JobApplicationResponse response = jobApplicationService.getApplicationById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateApplication(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody JobApplicationRequest request) {
        JobApplicationResponse response = jobApplicationService.updateApplication(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        JobApplicationResponse response = jobApplicationService.updateStatus(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            Authentication authentication,
            @PathVariable Long id) {
        jobApplicationService.deleteApplication(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "appliedDate");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && parts[1].trim().equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        // Whitelist allowable sort fields to prevent invalid property reference errors
        return switch (property) {
            case "appliedDate" -> Sort.by(direction, "appliedDate");
            case "jobTitle" -> Sort.by(direction, "jobTitle");
            case "status" -> Sort.by(direction, "status");
            case "deadline" -> Sort.by(direction, "deadline");
            default -> Sort.by(direction, "createdAt");
        };
    }
}

