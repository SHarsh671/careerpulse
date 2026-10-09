package com.portfolio.jobapp.controller;

import com.portfolio.jobapp.dto.response.DashboardStatsResponse;
import com.portfolio.jobapp.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<DashboardStatsResponse> getDashboard(Authentication authentication) {
        DashboardStatsResponse stats = dashboardService.getDashboardStats(authentication.getName());
        return ResponseEntity.ok(stats);
    }
}

