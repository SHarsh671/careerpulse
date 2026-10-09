package com.portfolio.jobapp.controller;

import com.portfolio.jobapp.dto.request.CompanyRequest;
import com.portfolio.jobapp.dto.response.CompanyResponse;
import com.portfolio.jobapp.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(Authentication authentication,
                                                         @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(authentication.getName(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies(Authentication authentication) {
        List<CompanyResponse> response = companyService.getAllCompanies(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(Authentication authentication,
                                                          @PathVariable Long id) {
        CompanyResponse response = companyService.getCompanyById(authentication.getName(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(Authentication authentication,
                                                         @PathVariable Long id,
                                                         @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.updateCompany(authentication.getName(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(Authentication authentication,
                                             @PathVariable Long id) {
        companyService.deleteCompany(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

