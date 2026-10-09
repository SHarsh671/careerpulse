package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.InterviewRequest;
import com.portfolio.jobapp.dto.response.InterviewResponse;
import com.portfolio.jobapp.entity.Interview;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.entity.enums.InterviewType;
import com.portfolio.jobapp.exception.ResourceNotFoundException;
import com.portfolio.jobapp.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {
    @Mock private InterviewRepository interviewRepository;
    @Mock private JobApplicationService jobApplicationService;
    @Mock private AuthService authService;
    @InjectMocks private InterviewService interviewService;

    private User user;
    private JobApplication application;
    private Interview interview;
    private InterviewRequest request;

    @BeforeEach
    void setUp() {
        user = new User(7L, "Taylor", "taylor@example.com", "hashed");
        application = new JobApplication();
        application.setId(11L);
        interview = new Interview(application, InterviewType.TECHNICAL,
                LocalDateTime.of(2026, 10, 10, 10, 0), "Jordan", "Algorithms", "PENDING");
        interview.setId(19L);
        request = new InterviewRequest(InterviewType.TECHNICAL, interview.getScheduledAt(),
                "Jordan", "Algorithms", "PENDING");
        when(authService.getUserByEmail("taylor@example.com")).thenReturn(user);
    }

    @Test
    void createInterviewChecksApplicationOwnershipAndMapsResponse() {
        when(jobApplicationService.getApplicationEntity(11L, 7L)).thenReturn(application);
        when(interviewRepository.save(any(Interview.class))).thenAnswer(invocation -> {
            Interview saved = invocation.getArgument(0);
            saved.setId(19L);
            return saved;
        });

        InterviewResponse result = interviewService.createInterview("taylor@example.com", 11L, request);

        assertThat(result.getId()).isEqualTo(19L);
        assertThat(result.getApplicationId()).isEqualTo(11L);
        assertThat(result.getType()).isEqualTo(InterviewType.TECHNICAL);
        verify(jobApplicationService).getApplicationEntity(11L, 7L);
    }

    @Test
    void listInterviewsVerifiesParentAndReturnsChronologicalResults() {
        when(jobApplicationService.getApplicationEntity(11L, 7L)).thenReturn(application);
        when(interviewRepository.findByApplicationIdAndApplicationUserIdOrderByScheduledAtAsc(11L, 7L))
                .thenReturn(List.of(interview));

        List<InterviewResponse> results = interviewService.getInterviewsForApplication("taylor@example.com", 11L);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getInterviewer()).isEqualTo("Jordan");
    }

    @Test
    void getInterviewReturnsUserScopedInterview() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.of(interview));

        assertThat(interviewService.getInterviewById("taylor@example.com", 19L).getNotes()).isEqualTo("Algorithms");
    }

    @Test
    void getInterviewHidesInterviewOwnedByAnotherUser() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.getInterviewById("taylor@example.com", 19L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateInterviewChangesFieldsAndSaves() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.of(interview));
        when(interviewRepository.save(interview)).thenReturn(interview);
        InterviewRequest update = new InterviewRequest(InterviewType.BEHAVIORAL,
                LocalDateTime.of(2026, 10, 11, 11, 0), "Morgan", "Leadership", "PASSED");

        InterviewResponse result = interviewService.updateInterview("taylor@example.com", 19L, update);

        assertThat(result.getType()).isEqualTo(InterviewType.BEHAVIORAL);
        assertThat(result.getInterviewer()).isEqualTo("Morgan");
        assertThat(result.getResult()).isEqualTo("PASSED");
        verify(interviewRepository).save(interview);
    }

    @Test
    void updateInterviewRejectsInterviewOwnedByAnotherUser() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.updateInterview("taylor@example.com", 19L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).save(any());
    }

    @Test
    void deleteInterviewDeletesOwnedInterview() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.of(interview));

        interviewService.deleteInterview("taylor@example.com", 19L);

        verify(interviewRepository).delete(interview);
    }

    @Test
    void deleteInterviewRejectsInterviewOwnedByAnotherUser() {
        when(interviewRepository.findByIdAndApplicationUserId(19L, 7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> interviewService.deleteInterview("taylor@example.com", 19L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).delete(any());
    }
}
