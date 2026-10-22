package com.ecoguard.dto;

import java.util.Map;

public class DashboardSummaryDto {

    private long totalReports;
    private long submittedReports;
    private long underReviewReports;
    private long inProgressReports;
    private long resolvedReports;
    private long rejectedReports;

    private Map<String, Long> categoryCounts;
    private Map<String, Long> zoneCounts;
    private double resolutionRatePercentage;

    public DashboardSummaryDto() {
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getSubmittedReports() {
        return submittedReports;
    }

    public void setSubmittedReports(long submittedReports) {
        this.submittedReports = submittedReports;
    }

    public long getUnderReviewReports() {
        return underReviewReports;
    }

    public void setUnderReviewReports(long underReviewReports) {
        this.underReviewReports = underReviewReports;
    }

    public long getInProgressReports() {
        return inProgressReports;
    }

    public void setInProgressReports(long inProgressReports) {
        this.inProgressReports = inProgressReports;
    }

    public long getResolvedReports() {
        return resolvedReports;
    }

    public void setResolvedReports(long resolvedReports) {
        this.resolvedReports = resolvedReports;
    }

    public long getRejectedReports() {
        return rejectedReports;
    }

    public void setRejectedReports(long rejectedReports) {
        this.rejectedReports = rejectedReports;
    }

    public Map<String, Long> getCategoryCounts() {
        return categoryCounts;
    }

    public void setCategoryCounts(Map<String, Long> categoryCounts) {
        this.categoryCounts = categoryCounts;
    }

    public Map<String, Long> getZoneCounts() {
        return zoneCounts;
    }

    public void setZoneCounts(Map<String, Long> zoneCounts) {
        this.zoneCounts = zoneCounts;
    }

    public double getResolutionRatePercentage() {
        return resolutionRatePercentage;
    }

    public void setResolutionRatePercentage(double resolutionRatePercentage) {
        this.resolutionRatePercentage = resolutionRatePercentage;
    }
}
