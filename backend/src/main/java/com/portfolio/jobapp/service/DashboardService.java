package com.portfolio.jobapp.service;

import com.portfolio.jobapp.dto.response.DashboardStatsResponse;
import com.portfolio.jobapp.dto.response.JobApplicationResponse;
import com.portfolio.jobapp.entity.JobApplication;
import com.portfolio.jobapp.entity.User;
import com.portfolio.jobapp.entity.enums.ApplicationStatus;
import com.portfolio.jobapp.repository.InterviewRepository;
import com.portfolio.jobapp.repository.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final JobApplicationRepository jobApplicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobApplicationService jobApplicationService;
    private final AuthService authService;

    public DashboardService(JobApplicationRepository jobApplicationRepository,
                            InterviewRepository interviewRepository,
                            JobApplicationService jobApplicationService,
                            AuthService authService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
        this.jobApplicationService = jobApplicationService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(String userEmail) {
        User user = authService.getUserByEmail(userEmail);
        Long userId = user.getId();

        long total = jobApplicationRepository.countByUserId(userId);
        long saved = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.SAVED);
        long applied = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.APPLIED);
        long oa = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OA);
        long interview = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEW);
        long finalInterview = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.FINAL_INTERVIEW);
        long offers = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFER);
        long rejections = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED);
        long withdrawn = jobApplicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.WITHDRAWN);

        // Applications submitted / in-flight (excluding only unsubmitted 'SAVED')
        long activePipeline = total - saved;

        // Applications that reached interview stage (OA, INTERVIEW, FINAL_INTERVIEW, OFFER)
        long interviewedOrOffered = oa + interview + finalInterview + offers;

        double interviewRate = 0.0;
        double offerRate = 0.0;

        if (activePipeline > 0) {
            interviewRate = BigDecimal.valueOf((double) interviewedOrOffered / activePipeline * 100)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();

            offerRate = BigDecimal.valueOf((double) offers / activePipeline * 100)
                    .setScale(1, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        List<JobApplication> recentEntities = jobApplicationRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId);
        List<JobApplicationResponse> recentApplications = recentEntities.stream()
                .map(app -> {
                    long interviewCount = interviewRepository.countByApplicationId(app.getId());
                    return jobApplicationService.mapToResponse(app, interviewCount);
                })
                .toList();

        Map<String, Long> statusBreakdown = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long count = switch (status) {
                case SAVED -> saved;
                case APPLIED -> applied;
                case OA -> oa;
                case INTERVIEW -> interview;
                case FINAL_INTERVIEW -> finalInterview;
                case OFFER -> offers;
                case REJECTED -> rejections;
                case WITHDRAWN -> withdrawn;
            };
            statusBreakdown.put(status.name(), count);
        }

        return new DashboardStatsResponse(
                total,
                saved,
                applied,
                oa,
                interview,
                finalInterview,
                offers,
                rejections,
                withdrawn,
                interviewRate,
                offerRate,
                recentApplications,
                statusBreakdown
        );
    }
}

