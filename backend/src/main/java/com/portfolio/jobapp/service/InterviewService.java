package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.InterviewRequest;
import com.portfolio.jobapp.dto.response.InterviewResponse;
import com.portfolio.jobapp.entity.Interview;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.exception.ResourceNotFoundException;
import com.portfolio.jobapp.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationService jobApplicationService;
    private final AuthService authService;

    public InterviewService(InterviewRepository interviewRepository,
                            JobApplicationService jobApplicationService,
                            AuthService authService) {
        this.interviewRepository = interviewRepository;
        this.jobApplicationService = jobApplicationService;
        this.authService = authService;
    }

    @Transactional
    public InterviewResponse createInterview(String userEmail, Long applicationId, InterviewRequest request) {
        User user = authService.getUserByEmail(userEmail);
        JobApplication application = jobApplicationService.getApplicationEntity(applicationId, user.getId());

        Interview interview = new Interview();
        interview.setApplication(application);
        interview.setType(request.getType());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setInterviewer(request.getInterviewer());
        interview.setNotes(request.getNotes());
        interview.setResult(request.getResult());

        Interview saved = interviewRepository.save(interview);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getInterviewsForApplication(String userEmail, Long applicationId) {
        User user = authService.getUserByEmail(userEmail);
        // Verify application belongs to this user
        jobApplicationService.getApplicationEntity(applicationId, user.getId());

        List<Interview> interviews = interviewRepository
                .findByApplicationIdAndApplicationUserIdOrderByScheduledAtAsc(applicationId, user.getId());

        return interviews.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InterviewResponse getInterviewById(String userEmail, Long interviewId) {
        User user = authService.getUserByEmail(userEmail);
        Interview interview = interviewRepository.findByIdAndApplicationUserId(interviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        return mapToResponse(interview);
    }

    @Transactional
    public InterviewResponse updateInterview(String userEmail, Long interviewId, InterviewRequest request) {
        User user = authService.getUserByEmail(userEmail);
        Interview interview = interviewRepository.findByIdAndApplicationUserId(interviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        interview.setType(request.getType());
        interview.setScheduledAt(request.getScheduledAt());
        interview.setInterviewer(request.getInterviewer());
        interview.setNotes(request.getNotes());
        interview.setResult(request.getResult());

        Interview updated = interviewRepository.save(interview);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteInterview(String userEmail, Long interviewId) {
        User user = authService.getUserByEmail(userEmail);
        Interview interview = interviewRepository.findByIdAndApplicationUserId(interviewId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Interview", "id", interviewId));

        interviewRepository.delete(interview);
    }

    private InterviewResponse mapToResponse(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getType(),
                interview.getScheduledAt(),
                interview.getInterviewer(),
                interview.getNotes(),
                interview.getResult(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }
}

