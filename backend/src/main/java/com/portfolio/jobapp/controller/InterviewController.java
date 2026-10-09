package com.portfolio.jobapp.controller;

import com.portfolio.jobapp.dto.request.InterviewRequest;
import com.portfolio.jobapp.dto.response.InterviewResponse;
import com.portfolio.jobapp.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<InterviewResponse> createInterview(
            Authentication authentication,
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.createInterview(authentication.getName(), applicationId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/applications/{applicationId}/interviews")
    public ResponseEntity<List<InterviewResponse>> getInterviewsForApplication(
            Authentication authentication,
            @PathVariable Long applicationId) {
        List<InterviewResponse> response = interviewService.getInterviewsForApplication(authentication.getName(), applicationId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/interviews/{id}")
    public ResponseEntity<InterviewResponse> getInterviewById(
            Authentication authentication,
            @PathVariable Long id) {
        InterviewResponse response = interviewService.getInterviewById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/interviews/{id}")
    public ResponseEntity<InterviewResponse> updateInterview(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response = interviewService.updateInterview(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/interviews/{id}")
    public ResponseEntity<Void> deleteInterview(
            Authentication authentication,
            @PathVariable Long id) {
        interviewService.deleteInterview(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

