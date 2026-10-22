package com.ecoguard.controller;

import com.ecoguard.dto.ApiResponseDto;
import com.ecoguard.dto.DashboardSummaryDto;
import com.ecoguard.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final ReportService reportService;

    public DashboardController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponseDto<DashboardSummaryDto>> getDashboardSummary() {
        DashboardSummaryDto summary = reportService.getDashboardSummary();
        return ResponseEntity.ok(ApiResponseDto.success("Dashboard metrics retrieved", summary));
    }
}
