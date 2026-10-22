package com.ecoguard.controller;

import com.ecoguard.dto.ApiResponseDto;
import com.ecoguard.dto.ReportRequestDto;
import com.ecoguard.dto.StatusUpdateRequestDto;
import com.ecoguard.model.StatusHistory;
import com.ecoguard.model.WasteReport;
import com.ecoguard.service.FileStorageService;
import com.ecoguard.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;
    private final FileStorageService fileStorageService;

    public ReportController(ReportService reportService, FileStorageService fileStorageService) {
        this.reportService = reportService;
        this.fileStorageService = fileStorageService;
    }

    /**
     * Submit a report via JSON payload
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDto<WasteReport>> submitReportJson(@Valid @RequestBody ReportRequestDto dto) {
        WasteReport created = reportService.createReport(dto);
        return new ResponseEntity<>(
                ApiResponseDto.success("Report submitted successfully with ID #" + created.getId(), created),
                HttpStatus.CREATED
        );
    }

    /**
     * Submit a report with an optional uploaded photo via multipart/form-data
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<WasteReport>> submitReportWithPhoto(
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "zone", required = false) String zone,
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "reporterName", required = false) String reporterName,
            @RequestParam(value = "reporterEmail", required = false) String reporterEmail,
            @RequestParam(value = "reporterPhone", required = false) String reporterPhone,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        ReportRequestDto dto = new ReportRequestDto();
        dto.setCategory(category);
        dto.setDescription(description);
        dto.setLocation(location);
        dto.setZone(zone);
        dto.setSeverity(severity);
        dto.setReporterName(reporterName);
        dto.setReporterEmail(reporterEmail);
        dto.setReporterPhone(reporterPhone);

        // Store photo safely if present
        if (photo != null && !photo.isEmpty()) {
            String photoUrl = fileStorageService.storePhoto(photo);
            dto.setImageUrl(photoUrl);
        }

        WasteReport created = reportService.createReport(dto);
        return new ResponseEntity<>(
                ApiResponseDto.success("Report submitted successfully with ID #" + created.getId(), created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<WasteReport>>> getReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String search) {
        List<WasteReport> reports = reportService.getReports(status, category, zone, search);
        return ResponseEntity.ok(ApiResponseDto.success("Retrieved " + reports.size() + " reports", reports));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponseDto<List<WasteReport>>> getCitizenNotifications(@RequestParam(required = false) String email) {
        List<WasteReport> list;
        if (email != null && !email.isBlank()) {
            list = reportService.getReportsByReporterEmail(email);
        } else {
            list = reportService.getReports("RESOLVED", null, null, null);
        }
        return ResponseEntity.ok(ApiResponseDto.success("Notifications retrieved", list));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<WasteReport>> getReportById(@PathVariable Long id) {
        Optional<WasteReport> reportOpt = reportService.getReportById(id);
        return reportOpt.map(wasteReport -> ResponseEntity.ok(ApiResponseDto.success("Report found", wasteReport)))
                .orElseGet(() -> new ResponseEntity<>(ApiResponseDto.error("Report with ID #" + id + " not found"), HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponseDto<List<StatusHistory>>> getReportHistory(@PathVariable Long id) {
        List<StatusHistory> history = reportService.getReportStatusHistory(id);
        return ResponseEntity.ok(ApiResponseDto.success("Status history retrieved", history));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<WasteReport>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequestDto dto) {
        Optional<WasteReport> updatedOpt = reportService.updateReportStatus(id, dto);
        return updatedOpt.map(wasteReport -> ResponseEntity.ok(ApiResponseDto.success("Report status updated to " + dto.getStatus(), wasteReport)))
                .orElseGet(() -> new ResponseEntity<>(ApiResponseDto.error("Report with ID #" + id + " not found"), HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteReport(@PathVariable Long id) {
        boolean deleted = reportService.deleteReport(id);
        if (deleted) {
            return ResponseEntity.ok(ApiResponseDto.success("Report #" + id + " successfully deleted"));
        } else {
            return new ResponseEntity<>(ApiResponseDto.error("Report #" + id + " not found"), HttpStatus.NOT_FOUND);
        }
    }
}
