package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.JobApplicationRequest;
import com.portfolio.jobapp.dto.request.UpdateStatusRequest;
import com.portfolio.jobapp.dto.response.CompanyResponse;
import com.portfolio.jobapp.dto.response.JobApplicationResponse;
import com.portfolio.jobapp.entity.Company;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.exception.BadRequestException;
import com.portfolio.jobapp.exception.ResourceNotFoundException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private CompanyService companyService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User sampleUser;
    private Company sampleCompany;
    private JobApplication sampleApplication;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Developer", "alice@example.com", "secret");
        sampleCompany = new Company(sampleUser, "Google", "https://google.com", "Tech", "Mountain View", "Top choice");
        sampleCompany.setId(10L);

        sampleApplication = new JobApplication(
                sampleUser,
                sampleCompany,
                "Software Engineer",
                "Mountain View, CA",
                "https://careers.google.com/job/123",
                ApplicationStatus.APPLIED,
                140000,
                180000,
                LocalDate.now(),
                null,
                "Referral from college alumni"
        );
        sampleApplication.setId(100L);
    }

    @Test
    @DisplayName("Should create job application when input is valid and company belongs to user")
    void createApplication_Success() {
        JobApplicationRequest request = new JobApplicationRequest(
                10L, "Software Engineer", "Mountain View, CA",
                "https://careers.google.com/job/123", ApplicationStatus.APPLIED,
                140000, 180000, LocalDate.now(), null, "Referral"
        );

        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyService.getCompanyEntity(10L, 1L)).thenReturn(sampleCompany);
        when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(sampleApplication);
        when(companyService.mapToCompanyResponse(sampleCompany, 0))
                .thenReturn(new CompanyResponse(10L, "Google", "https://google.com", "Tech", "Mountain View", "", 0, null, null));

        JobApplicationResponse response = jobApplicationService.createApplication("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getJobTitle()).isEqualTo("Software Engineer");
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(response.getSalaryMin()).isEqualTo(140000);
        assertThat(response.getSalaryMax()).isEqualTo(180000);

        verify(jobApplicationRepository).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should reject application creation when salaryMin exceeds salaryMax")
    void createApplication_InvalidSalaries_ThrowsBadRequest() {
        JobApplicationRequest request = new JobApplicationRequest(
                10L, "Software Engineer", "Remote",
                null, ApplicationStatus.APPLIED,
                200000, 100000, null, null, null
        );

        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyService.getCompanyEntity(10L, 1L)).thenReturn(sampleCompany);

        assertThatThrownBy(() -> jobApplicationService.createApplication("alice@example.com", request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Minimum salary (200000) cannot exceed maximum salary (100000)");

        verify(jobApplicationRepository, never()).save(any(JobApplication.class));
    }

    @Test
    @DisplayName("Should retrieve user's application with interview count")
    void getApplicationById_Success() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleApplication));
        when(interviewRepository.countByApplicationId(100L)).thenReturn(2L);
        when(companyService.mapToCompanyResponse(sampleCompany, 0))
                .thenReturn(new CompanyResponse(10L, "Google", null, null, null, null, 0, null, null));

        JobApplicationResponse response = jobApplicationService.getApplicationById("alice@example.com", 100L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getInterviewCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when application does not belong to user")
    void getApplicationById_NotFoundOrUnauthorized_ThrowsNotFound() {
        when(authService.getUserByEmail("bob@example.com")).thenReturn(new User(2L, "Bob", "bob@example.com", "pwd"));
        when(jobApplicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.getApplicationById("bob@example.com", 100L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should update status of application")
    void updateStatus_Success() {
        UpdateStatusRequest statusRequest = new UpdateStatusRequest(ApplicationStatus.INTERVIEW);

        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleApplication));
        when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(interviewRepository.countByApplicationId(100L)).thenReturn(1L);
        when(companyService.mapToCompanyResponse(any(), anyLong()))
                .thenReturn(new CompanyResponse(10L, "Google", null, null, null, null, 0, null, null));

        JobApplicationResponse response = jobApplicationService.updateStatus("alice@example.com", 100L, statusRequest);

        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.INTERVIEW);
        verify(jobApplicationRepository).save(sampleApplication);
    }

    @Test
    @DisplayName("Should delete application and cascade interview deletion")
    void deleteApplication_CascadesInterviews() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleApplication));

        jobApplicationService.deleteApplication("alice@example.com", 100L);

        verify(interviewRepository).deleteByApplicationId(100L);
        verify(jobApplicationRepository).delete(sampleApplication);
    }

    @Test
    void updateApplicationChangesFieldsAndRetainsExistingStatusWhenStatusOmitted() {
        JobApplicationRequest request = new JobApplicationRequest(
                10L, " Staff Engineer ", "Remote", null, null,
                null, null, null, null, "Updated notes");
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleApplication));
        when(companyService.getCompanyEntity(10L, 1L)).thenReturn(sampleCompany);
        when(jobApplicationRepository.save(sampleApplication)).thenReturn(sampleApplication);
        when(interviewRepository.countByApplicationId(100L)).thenReturn(3L);
        when(companyService.mapToCompanyResponse(sampleCompany, 0))
                .thenReturn(new CompanyResponse(10L, "Google", null, null, null, null, 0, null, null));

        JobApplicationResponse result = jobApplicationService.updateApplication("alice@example.com", 100L, request);

        assertThat(result.getJobTitle()).isEqualTo("Staff Engineer");
        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPLIED);
        assertThat(result.getInterviewCount()).isEqualTo(3L);
    }

    @Test
    void updateApplicationRejectsInvalidSalaryRange() {
        JobApplicationRequest request = new JobApplicationRequest(
                10L, "Engineer", null, null, ApplicationStatus.APPLIED,
                200000, 100000, null, null, null);
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(jobApplicationRepository.findByIdAndUserId(100L, 1L)).thenReturn(Optional.of(sampleApplication));
        when(companyService.getCompanyEntity(10L, 1L)).thenReturn(sampleCompany);

        assertThatThrownBy(() -> jobApplicationService.updateApplication("alice@example.com", 100L, request))
                .isInstanceOf(BadRequestException.class);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void updateStatusReturnsNotFoundForAnotherUsersApplication() {
        when(authService.getUserByEmail("bob@example.com"))
                .thenReturn(new User(2L, "Bob", "bob@example.com", "pwd"));
        when(jobApplicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.updateStatus("bob@example.com", 100L,
                new UpdateStatusRequest(ApplicationStatus.REJECTED)))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(jobApplicationRepository, never()).save(any());
    }

    @Test
    void deleteApplicationReturnsNotFoundForAnotherUsersApplication() {
        when(authService.getUserByEmail("bob@example.com"))
                .thenReturn(new User(2L, "Bob", "bob@example.com", "pwd"));
        when(jobApplicationRepository.findByIdAndUserId(100L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobApplicationService.deleteApplication("bob@example.com", 100L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(interviewRepository, never()).deleteByApplicationId(anyLong());
    }
}

