package com.ecoguard.service;

import com.ecoguard.dto.DashboardSummaryDto;
import com.ecoguard.dto.ReportRequestDto;
import com.ecoguard.dto.StatusUpdateRequestDto;
import com.ecoguard.model.StatusHistory;
import com.ecoguard.model.WasteReport;
import com.ecoguard.repository.StatusHistoryRepository;
import com.ecoguard.repository.WasteReportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReportService {

    private final WasteReportRepository reportRepository;
    private final StatusHistoryRepository statusHistoryRepository;

    public ReportService(WasteReportRepository reportRepository, StatusHistoryRepository statusHistoryRepository) {
        this.reportRepository = reportRepository;
        this.statusHistoryRepository = statusHistoryRepository;
    }

    @Transactional
    public WasteReport createReport(ReportRequestDto dto) {
        WasteReport report = new WasteReport();
        report.setCategory(dto.getCategory());
        report.setDescription(dto.getDescription());
        report.setLocation(dto.getLocation());
        report.setZone(dto.getZone() != null && !dto.getZone().isBlank() ? dto.getZone() : "General Marshland");
        report.setSeverity(dto.getSeverity() != null && !dto.getSeverity().isBlank() ? dto.getSeverity() : "MEDIUM");
        report.setStatus("SUBMITTED");
        report.setReporterName(dto.getReporterName() != null && !dto.getReporterName().isBlank() ? dto.getReporterName() : "Anonymous Citizen");
        report.setReporterEmail(dto.getReporterEmail());
        report.setReporterPhone(dto.getReporterPhone());
        report.setImageUrl(dto.getImageUrl());

        WasteReport saved = reportRepository.save(report);

        // Record initial status in history
        StatusHistory history = new StatusHistory(
                saved.getId(),
                null,
                "SUBMITTED",
                saved.getReporterName(),
                "Initial report filed by citizen"
        );
        statusHistoryRepository.save(history);

        return saved;
    }

    public List<WasteReport> getReports(String status, String category, String zone, String search) {
        String cleanStatus = (status != null && !status.equalsIgnoreCase("ALL")) ? status.trim() : null;
        String cleanCategory = (category != null && !category.equalsIgnoreCase("ALL")) ? category.trim() : null;
        String cleanZone = (zone != null && !zone.equalsIgnoreCase("ALL")) ? zone.trim() : null;
        String cleanSearch = (search != null && !search.isBlank()) ? search.trim() : null;

        return reportRepository.filterReports(cleanStatus, cleanCategory, cleanZone, cleanSearch);
    }

    public List<WasteReport> getReportsByReporterEmail(String email) {
        if (email == null || email.isBlank()) {
            return Collections.emptyList();
        }
        return reportRepository.findByReporterEmailIgnoreCaseOrderByCreatedAtDesc(email.trim());
    }

    public Optional<WasteReport> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    @Transactional
    public Optional<WasteReport> updateReportStatus(Long id, StatusUpdateRequestDto dto) {
        Optional<WasteReport> opt = reportRepository.findById(id);
        if (opt.isEmpty()) {
            return Optional.empty();
        }

        String role = dto.getUserRole() != null
                ? dto.getUserRole().toUpperCase().trim()
                : "";

        if (!role.equals("ADMIN") && !role.equals("VOLUNTEER") && !role.equals("CITIZEN")) {
            throw new IllegalArgumentException("A valid user role is required.");
        }
        String newStatus = dto.getStatus().toUpperCase().trim();

        // Role Permission Validation:
        // 1. Citizen cannot alter report statuses
        if ("CITIZEN".equals(role)) {
            throw new IllegalArgumentException("Citizens cannot update report status. You can submit new reports and monitor their remediation.");
        }

        // 2. Volunteer can review and post cleanup progress updates (UNDER_REVIEW, IN_PROGRESS)
        // Admin alone marks reports completed (RESOLVED or REJECTED)
        if ("VOLUNTEER".equals(role)) {
            if ("RESOLVED".equals(newStatus) || "REJECTED".equals(newStatus)) {
                throw new IllegalArgumentException("Volunteers cannot mark reports as completed. Only Administrators can mark reports as RESOLVED or REJECTED.");
            }
        }

        WasteReport report = opt.get();
        String oldStatus = report.getStatus();

        report.setStatus(newStatus);
        WasteReport updated = reportRepository.save(report);

        // Record audit trail
        String updatedBy = dto.getUpdatedBy() != null && !dto.getUpdatedBy().isBlank()
                ? dto.getUpdatedBy()
                : ("VOLUNTEER".equals(role) ? "Eco Volunteer Patrol" : "Wetland Authority Administrator");

        StatusHistory history = new StatusHistory(
                id,
                oldStatus,
                newStatus,
                updatedBy,
                dto.getComment() != null ? dto.getComment() : ("Status updated to " + newStatus)
        );
        statusHistoryRepository.save(history);

        return Optional.of(updated);
    }

    @Transactional
    public boolean deleteReport(Long id) {
        if (reportRepository.existsById(id)) {
            statusHistoryRepository.deleteByReportId(id);
            reportRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<StatusHistory> getReportStatusHistory(Long reportId) {
        return statusHistoryRepository.findByReportIdOrderByUpdatedAtDesc(reportId);
    }

    public DashboardSummaryDto getDashboardSummary() {
        DashboardSummaryDto summary = new DashboardSummaryDto();

        long total = reportRepository.count();
        long submitted = reportRepository.countByStatus("SUBMITTED");
        long underReview = reportRepository.countByStatus("UNDER_REVIEW");
        long inProgress = reportRepository.countByStatus("IN_PROGRESS");
        long resolved = reportRepository.countByStatus("RESOLVED");
        long rejected = reportRepository.countByStatus("REJECTED");

        summary.setTotalReports(total);
        summary.setSubmittedReports(submitted);
        summary.setUnderReviewReports(underReview);
        summary.setInProgressReports(inProgress);
        summary.setResolvedReports(resolved);
        summary.setRejectedReports(rejected);

        double rate = total > 0 ? ((double) resolved / total) * 100.0 : 0.0;
        summary.setResolutionRatePercentage(Math.round(rate * 10.0) / 10.0);

        // Category breakdown
        Map<String, Long> categoryMap = new LinkedHashMap<>();
        List<Object[]> catResults = reportRepository.countReportsByCategory();
        for (Object[] row : catResults) {
            String cat = (String) row[0];
            Long count = (Long) row[1];
            if (cat != null) {
                categoryMap.put(cat, count);
            }
        }
        summary.setCategoryCounts(categoryMap);

        // Zone breakdown
        Map<String, Long> zoneMap = new LinkedHashMap<>();
        List<Object[]> zoneResults = reportRepository.countReportsByZone();
        for (Object[] row : zoneResults) {
            String z = (String) row[0];
            Long count = (Long) row[1];
            if (z != null) {
                zoneMap.put(z, count);
            }
        }
        summary.setZoneCounts(zoneMap);

        return summary;
    }
}
