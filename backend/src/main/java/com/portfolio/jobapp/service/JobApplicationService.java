package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.JobApplicationRequest;
import com.portfolio.jobapp.dto.request.UpdateStatusRequest;
import com.portfolio.jobapp.dto.response.CompanyResponse;
import com.portfolio.jobapp.dto.response.JobApplicationResponse;
import com.portfolio.jobapp.dto.response.PagedResponse;
import com.portfolio.jobapp.entity.Company;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.exception.BadRequestException;
import com.portfolio.jobapp.exception.ResourceNotFoundException;
import com.portfolio.jobapp.repository.InterviewRepository;
import com.portfolio.jobapp.repository.JobApplicationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class JobApplicationService {

    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;
    private final CompanyService companyService;
    private final AuthService authService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 InterviewRepository interviewRepository,
                                 CompanyService companyService,
                                 AuthService authService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
        this.companyService = companyService;
        this.authService = authService;
    }

    @Transactional
    public JobApplicationResponse createApplication(String userEmail, JobApplicationRequest request) {
        User user = authService.getUserByEmail(userEmail);
        Company company = companyService.getCompanyEntity(request.getCompanyId(), user.getId());

        validateSalaries(request.getSalaryMin(), request.getSalaryMax());

        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setCompany(company);
        application.setJobTitle(request.getJobTitle().trim());
        application.setLocation(request.getLocation());
        application.setJobUrl(request.getJobUrl());
        application.setStatus(request.getStatus() != null ? request.getStatus() : ApplicationStatus.SAVED);
        application.setSalaryMin(request.getSalaryMin());
        application.setSalaryMax(request.getSalaryMax());
        application.setAppliedDate(request.getAppliedDate());
        application.setDeadline(request.getDeadline());
        application.setNotes(request.getNotes());

        JobApplication saved = jobApplicationRepository.save(application);
        return mapToResponse(saved, 0);
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getApplicationById(String userEmail, Long applicationId) {
        User user = authService.getUserByEmail(userEmail);
        JobApplication application = getApplicationEntity(applicationId, user.getId());
        long interviewCount = interviewRepository.countByApplicationId(application.getId());

        return mapToResponse(application, interviewCount);
    }

    @Transactional(readOnly = true)
    public PagedResponse<JobApplicationResponse> getApplications(
            String userEmail,
            ApplicationStatus status,
            Long companyId,
            String location,
            String search,
            Pageable pageable) {

        User user = authService.getUserByEmail(userEmail);

        Specification<JobApplication> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always enforce user isolation
            predicates.add(cb.equal(root.get("user").get("id"), user.getId()));

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (companyId != null) {
                predicates.add(cb.equal(root.get("company").get("id"), companyId));
            }

            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase().trim() + "%"));
            }

            if (StringUtils.hasText(search)) {
                String term = "%" + search.toLowerCase().trim() + "%";
                Predicate titlePredicate = cb.like(cb.lower(root.get("jobTitle")), term);
                Predicate companyNamePredicate = cb.like(cb.lower(root.get("company").get("name")), term);
                Predicate locationPredicate = cb.like(cb.lower(root.get("location")), term);
                predicates.add(cb.or(titlePredicate, companyNamePredicate, locationPredicate));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<JobApplication> pageResult = jobApplicationRepository.findAll(spec, pageable);

        List<JobApplicationResponse> responses = pageResult.getContent().stream()
                .map(app -> {
                    long interviewCount = interviewRepository.countByApplicationId(app.getId());
                    return mapToResponse(app, interviewCount);
                })
                .toList();

        return new PagedResponse<>(
                responses,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }

    @Transactional
    public JobApplicationResponse updateApplication(String userEmail, Long applicationId, JobApplicationRequest request) {
        User user = authService.getUserByEmail(userEmail);
        JobApplication application = getApplicationEntity(applicationId, user.getId());
        Company company = companyService.getCompanyEntity(request.getCompanyId(), user.getId());

        validateSalaries(request.getSalaryMin(), request.getSalaryMax());

        application.setCompany(company);
        application.setJobTitle(request.getJobTitle().trim());
        application.setLocation(request.getLocation());
        application.setJobUrl(request.getJobUrl());
        if (request.getStatus() != null) {
            application.setStatus(request.getStatus());
        }
        application.setSalaryMin(request.getSalaryMin());
        application.setSalaryMax(request.getSalaryMax());
        application.setAppliedDate(request.getAppliedDate());
        application.setDeadline(request.getDeadline());
        application.setNotes(request.getNotes());

        JobApplication updated = jobApplicationRepository.save(application);
        long interviewCount = interviewRepository.countByApplicationId(updated.getId());
        return mapToResponse(updated, interviewCount);
    }

    @Transactional
    public JobApplicationResponse updateStatus(String userEmail, Long applicationId, UpdateStatusRequest request) {
        User user = authService.getUserByEmail(userEmail);
        JobApplication application = getApplicationEntity(applicationId, user.getId());

        application.setStatus(request.getStatus());
        JobApplication updated = jobApplicationRepository.save(application);

        long interviewCount = interviewRepository.countByApplicationId(updated.getId());
        return mapToResponse(updated, interviewCount);
    }

    @Transactional
    public void deleteApplication(String userEmail, Long applicationId) {
        User user = authService.getUserByEmail(userEmail);
        JobApplication application = getApplicationEntity(applicationId, user.getId());

        // First remove all child interviews
        interviewRepository.deleteByApplicationId(applicationId);
        jobApplicationRepository.delete(application);
    }

    public JobApplication getApplicationEntity(Long applicationId, Long userId) {
        return jobApplicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("JobApplication", "id", applicationId));
    }

    private void validateSalaries(Integer min, Integer max) {
        if (min != null && max != null && min > max) {
            throw new BadRequestException("Minimum salary (" + min + ") cannot exceed maximum salary (" + max + ")");
        }
    }

    public JobApplicationResponse mapToResponse(JobApplication app, long interviewCount) {
        CompanyResponse companyResponse = companyService.mapToCompanyResponse(app.getCompany(), 0);

        return new JobApplicationResponse(
                app.getId(),
                app.getJobTitle(),
                companyResponse,
                app.getLocation(),
                app.getJobUrl(),
                app.getStatus(),
                app.getSalaryMin(),
                app.getSalaryMax(),
                app.getAppliedDate(),
                app.getDeadline(),
                app.getNotes(),
                interviewCount,
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}

