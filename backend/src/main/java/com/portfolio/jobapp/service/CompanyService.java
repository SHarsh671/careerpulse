package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.request.CompanyRequest;
import com.portfolio.jobapp.dto.response.CompanyResponse;
import com.portfolio.jobapp.entity.Company;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.exception.BadRequestException;
import com.portfolio.jobapp.exception.DuplicateResourceException;
import com.portfolio.jobapp.exception.ResourceNotFoundException;
import com.portfolio.jobapp.repository.CompanyRepository;
import com.portfolio.jobapp.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final AuthService authService;

    public CompanyService(CompanyRepository companyRepository,
                          JobApplicationRepository jobApplicationRepository,
                          AuthService authService) {
        this.companyRepository = companyRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.authService = authService;
    }

    @Transactional
    public CompanyResponse createCompany(String userEmail, CompanyRequest request) {
        User user = authService.getUserByEmail(userEmail);

        String companyName = request.getName().trim();
        if (companyRepository.existsByNameIgnoreCaseAndUserId(companyName, user.getId())) {
            throw new DuplicateResourceException("Company with name '" + companyName + "' already exists");
        }

        Company company = new Company();
        company.setUser(user);
        company.setName(companyName);
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setLocation(request.getLocation());
        company.setNotes(request.getNotes());

        Company savedCompany = companyRepository.save(company);
        return mapToCompanyResponse(savedCompany, 0);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> getAllCompanies(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        List<Company> companies = companyRepository.findByUserIdOrderByNameAsc(user.getId());

        return companies.stream()
                .map(c -> {
                    long appCount = companyRepository.countApplicationsByCompanyId(c.getId());
                    return mapToCompanyResponse(c, appCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(String userEmail, Long companyId) {
        User user = authService.getUserByEmail(userEmail);
        Company company = getCompanyEntity(companyId, user.getId());
        long appCount = companyRepository.countApplicationsByCompanyId(company.getId());

        return mapToCompanyResponse(company, appCount);
    }

    @Transactional
    public CompanyResponse updateCompany(String userEmail, Long companyId, CompanyRequest request) {
        User user = authService.getUserByEmail(userEmail);
        Company company = getCompanyEntity(companyId, user.getId());

        String newName = request.getName().trim();
        if (companyRepository.existsByNameIgnoreCaseAndUserIdAndIdNot(newName, user.getId(), companyId)) {
            throw new DuplicateResourceException("Another company with name '" + newName + "' already exists");
        }

        company.setName(newName);
        company.setWebsite(request.getWebsite());
        company.setIndustry(request.getIndustry());
        company.setLocation(request.getLocation());
        company.setNotes(request.getNotes());

        Company updatedCompany = companyRepository.save(company);
        long appCount = companyRepository.countApplicationsByCompanyId(updatedCompany.getId());
        return mapToCompanyResponse(updatedCompany, appCount);
    }

    @Transactional
    public void deleteCompany(String userEmail, Long companyId) {
        User user = authService.getUserByEmail(userEmail);
        Company company = getCompanyEntity(companyId, user.getId());

        long applicationCount = jobApplicationRepository.countByCompanyId(companyId);
        if (applicationCount > 0) {
            throw new BadRequestException("Cannot delete company '" + company.getName() +
                    "' because it has " + applicationCount + " associated job application(s). Delete those applications first.");
        }

        companyRepository.delete(company);
    }

    public Company getCompanyEntity(Long companyId, Long userId) {
        return companyRepository.findByIdAndUserId(companyId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", "id", companyId));
    }

    public CompanyResponse mapToCompanyResponse(Company company, long applicationCount) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getWebsite(),
                company.getIndustry(),
                company.getLocation(),
                company.getNotes(),
                applicationCount,
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }
}

