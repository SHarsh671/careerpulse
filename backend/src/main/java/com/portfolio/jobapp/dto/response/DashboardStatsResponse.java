package com.portfolio.jobapp.dto.response;

import java.util.List;
import java.util.Map;

public class DashboardStatsResponse {

    private long totalApplications;
    private long savedApplications;
    private long appliedApplications;
    private long oaCount;
    private long interviewCount;
    private long finalInterviewCount;
    private long offers;
    private long rejections;
    private long withdrawn;
    private double interviewRate;
    private double offerRate;
    private List<JobApplicationResponse> recentApplications;
    private Map<String, Long> statusBreakdown;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(long totalApplications, long savedApplications,
                                  long appliedApplications, long oaCount, long interviewCount,
                                  long finalInterviewCount, long offers, long rejections,
                                  long withdrawn, double interviewRate, double offerRate,
                                  List<JobApplicationResponse> recentApplications,
                                  Map<String, Long> statusBreakdown) {
        this.totalApplications = totalApplications;
        this.savedApplications = savedApplications;
        this.appliedApplications = appliedApplications;
        this.oaCount = oaCount;
        this.interviewCount = interviewCount;
        this.finalInterviewCount = finalInterviewCount;
        this.offers = offers;
        this.rejections = rejections;
        this.withdrawn = withdrawn;
        this.interviewRate = interviewRate;
        this.offerRate = offerRate;
        this.recentApplications = recentApplications;
        this.statusBreakdown = statusBreakdown;
    }

    public long getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(long totalApplications) {
        this.totalApplications = totalApplications;
    }

    public long getSavedApplications() {
        return savedApplications;
    }

    public void setSavedApplications(long savedApplications) {
        this.savedApplications = savedApplications;
    }

    public long getAppliedApplications() {
        return appliedApplications;
    }

    public void setAppliedApplications(long appliedApplications) {
        this.appliedApplications = appliedApplications;
    }

    public long getOaCount() {
        return oaCount;
    }

    public void setOaCount(long oaCount) {
        this.oaCount = oaCount;
    }

    public long getInterviewCount() {
        return interviewCount;
    }

    public void setInterviewCount(long interviewCount) {
        this.interviewCount = interviewCount;
    }

    public long getFinalInterviewCount() {
        return finalInterviewCount;
    }

    public void setFinalInterviewCount(long finalInterviewCount) {
        this.finalInterviewCount = finalInterviewCount;
    }

    public long getOffers() {
        return offers;
    }

    public void setOffers(long offers) {
        this.offers = offers;
    }

    public long getRejections() {
        return rejections;
    }

    public void setRejections(long rejections) {
        this.rejections = rejections;
    }

    public long getWithdrawn() {
        return withdrawn;
    }

    public void setWithdrawn(long withdrawn) {
        this.withdrawn = withdrawn;
    }

    public double getInterviewRate() {
        return interviewRate;
    }

    public void setInterviewRate(double interviewRate) {
        this.interviewRate = interviewRate;
    }

    public double getOfferRate() {
        return offerRate;
    }

    public void setOfferRate(double offerRate) {
        this.offerRate = offerRate;
    }

    public List<JobApplicationResponse> getRecentApplications() {
        return recentApplications;
    }

    public void setRecentApplications(List<JobApplicationResponse> recentApplications) {
        this.recentApplications = recentApplications;
    }

    public Map<String, Long> getStatusBreakdown() {
        return statusBreakdown;
    }

    public void setStatusBreakdown(Map<String, Long> statusBreakdown) {
        this.statusBreakdown = statusBreakdown;
    }
}

