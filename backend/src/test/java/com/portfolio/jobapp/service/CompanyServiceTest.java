package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.CompanyRequest;
import com.portfolio.jobapp.dto.response.CompanyResponse;
import com.portfolio.jobapp.entity.Company;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.exception.BadRequestException;
import com.portfolio.jobapp.exception.DuplicateResourceException;
import com.portfolio.jobapp.repository.CompanyRepository;
import com.portfolio.jobapp.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CompanyService companyService;

    private User sampleUser;
    private Company sampleCompany;

    @BeforeEach
    void setUp() {
        sampleUser = new User(1L, "Alice Developer", "alice@example.com", "pwd");
        sampleCompany = new Company(sampleUser, "Stripe", "https://stripe.com", "Fintech", "San Francisco", "Notes");
        sampleCompany.setId(5L);
    }

    @Test
    @DisplayName("Should successfully create a company for user")
    void createCompany_Success() {
        CompanyRequest request = new CompanyRequest("Stripe", "https://stripe.com", "Fintech", "San Francisco", "Notes");

        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.existsByNameIgnoreCaseAndUserId("Stripe", 1L)).thenReturn(false);
        when(companyRepository.save(any(Company.class))).thenReturn(sampleCompany);

        CompanyResponse response = companyService.createCompany("alice@example.com", request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Stripe");
        verify(companyRepository).save(any(Company.class));
    }

    @Test
    @DisplayName("Should prevent creating duplicate company for same user")
    void createCompany_DuplicateName_ThrowsException() {
        CompanyRequest request = new CompanyRequest("Stripe", "https://stripe.com", "Fintech", "San Francisco", "Notes");

        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.existsByNameIgnoreCaseAndUserId("Stripe", 1L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.createCompany("alice@example.com", request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");

        verify(companyRepository, never()).save(any(Company.class));
    }

    @Test
    @DisplayName("Should prevent company deletion if active job applications are attached")
    void deleteCompany_ActiveApplications_ThrowsBadRequest() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(sampleCompany));
        when(jobApplicationRepository.countByCompanyId(5L)).thenReturn(3L);

        assertThatThrownBy(() -> companyService.deleteCompany("alice@example.com", 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("has 3 associated job application(s)");

        verify(companyRepository, never()).delete(any(Company.class));
    }

    @Test
    void getAllCompaniesReturnsOnlyRowsForAuthenticatedUser() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByUserIdOrderByNameAsc(1L)).thenReturn(List.of(sampleCompany));
        when(companyRepository.countApplicationsByCompanyId(5L)).thenReturn(2L);

        List<CompanyResponse> result = companyService.getAllCompanies("alice@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getApplicationCount()).isEqualTo(2L);
    }

    @Test
    void getCompanyByIdUsesOwnerScopedLookup() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.countApplicationsByCompanyId(5L)).thenReturn(1L);

        assertThat(companyService.getCompanyById("alice@example.com", 5L).getApplicationCount()).isEqualTo(1L);
        verify(companyRepository).findByIdAndUserId(5L, 1L);
    }

    @Test
    void updateCompanyChangesFieldsAndReportsApplicationCount() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.existsByNameIgnoreCaseAndUserIdAndIdNot("Stripe Inc", 1L, 5L)).thenReturn(false);
        when(companyRepository.save(sampleCompany)).thenReturn(sampleCompany);
        when(companyRepository.countApplicationsByCompanyId(5L)).thenReturn(4L);

        CompanyResponse result = companyService.updateCompany("alice@example.com", 5L,
                new CompanyRequest(" Stripe Inc ", "https://stripe.com", "Fintech", "Remote", "Updated"));

        assertThat(result.getName()).isEqualTo("Stripe Inc");
        assertThat(result.getLocation()).isEqualTo("Remote");
        assertThat(result.getApplicationCount()).isEqualTo(4L);
    }

    @Test
    void updateCompanyRejectsDuplicateName() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(sampleCompany));
        when(companyRepository.existsByNameIgnoreCaseAndUserIdAndIdNot("Square", 1L, 5L)).thenReturn(true);

        assertThatThrownBy(() -> companyService.updateCompany("alice@example.com", 5L,
                new CompanyRequest("Square", null, null, null, null)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(companyRepository, never()).save(any());
    }

    @Test
    void deleteCompanyDeletesWhenNoApplicationsExist() {
        when(authService.getUserByEmail("alice@example.com")).thenReturn(sampleUser);
        when(companyRepository.findByIdAndUserId(5L, 1L)).thenReturn(Optional.of(sampleCompany));
        when(jobApplicationRepository.countByCompanyId(5L)).thenReturn(0L);

        companyService.deleteCompany("alice@example.com", 5L);

        verify(companyRepository).delete(sampleCompany);
    }
}

