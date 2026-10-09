package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.response.DashboardStatsResponse;
import com.portfolio.jobapp.entity.Company;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.repository.InterviewRepository;
import com.portfolio.jobapp.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobApplicationService jobApplicationService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private DashboardService dashboardService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Developer", "alice@example.com", "secret");
    }

    @Test
    @DisplayName("Should accurately calculate dashboard statistics, interview rate, and offer rate")
    void getDashboardStats_CalculationsAreCorrect() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);

        // Given:
        // Total = 10 applications
        // 2 SAVED (not yet submitted) -> activePipeline = 10 - 2 = 8
        // 3 APPLIED
        // 1 OA
        // 2 INTERVIEW
        // 1 OFFER
        // 1 REJECTED
        // interviewedOrOffered = OA(1) + INTERVIEW(2) + FINAL(0) + OFFER(1) = 4
        // interviewRate = 4 / 8 * 100 = 50.0%
        // offerRate = 1 / 8 * 100 = 12.5%

        when(jobApplicationRepository.countByUserId(1L)).thenReturn(10L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.SAVED)).thenReturn(2L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.APPLIED)).thenReturn(3L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.OA)).thenReturn(1L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.INTERVIEW)).thenReturn(2L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.FINAL_INTERVIEW)).thenReturn(0L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.OFFER)).thenReturn(1L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.REJECTED)).thenReturn(1L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.WITHDRAWN)).thenReturn(0L);

        when(jobApplicationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        DashboardStatsResponse stats = dashboardService.getDashboardStats("alice@example.com");

        assertThat(stats.getTotalApplications()).isEqualTo(10L);
        assertThat(stats.getSavedApplications()).isEqualTo(2L);
        assertThat(stats.getAppliedApplications()).isEqualTo(3L);
        assertThat(stats.getOaCount()).isEqualTo(1L);
        assertThat(stats.getInterviewCount()).isEqualTo(2L);
        assertThat(stats.getOffers()).isEqualTo(1L);
        assertThat(stats.getRejections()).isEqualTo(1L);
        assertThat(stats.getInterviewRate()).isEqualTo(50.0);
        assertThat(stats.getOfferRate()).isEqualTo(12.5);
        assertThat(stats.getStatusBreakdown()).containsEntry("APPLIED", 3L);
        assertThat(stats.getStatusBreakdown()).containsEntry("OFFER", 1L);
    }

    @Test
    @DisplayName("Should return 0.0 rates when user has zero active applications")
    void getDashboardStats_ZeroApplications_ZeroRates() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.countByUserId(1L)).thenReturn(1L);
        when(jobApplicationRepository.countByUserIdAndStatus(1L, ApplicationStatus.SAVED)).thenReturn(1L);
        when(jobApplicationRepository.findTop5ByUserIdOrderByCreatedAtDesc(1L)).thenReturn(Collections.emptyList());

        DashboardStatsResponse stats = dashboardService.getDashboardStats("alice@example.com");

        assertThat(stats.getInterviewRate()).isEqualTo(0.0);
        assertThat(stats.getOfferRate()).isEqualTo(0.0);
    }
}

