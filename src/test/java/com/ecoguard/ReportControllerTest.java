package com.ecoguard;

import com.ecoguard.controller.ReportController;
import com.ecoguard.dto.ApiResponseDto;
import com.ecoguard.dto.ReportRequestDto;
import com.ecoguard.dto.StatusUpdateRequestDto;
import com.ecoguard.model.StatusHistory;
import com.ecoguard.model.WasteReport;
import com.ecoguard.repository.WasteReportRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class ReportControllerTest {

    @Autowired
    private ReportController reportController;

    @Autowired
    private WasteReportRepository reportRepository;

    @Autowired
    private Validator validator;

    @Test
    @DisplayName("POST /api/reports - Valid report saves to database and returns 201 CREATED")
    void testCreateReportSuccess() {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setCategory("Plastic Waste");
        dto.setDescription("Testing single-use plastic bottles detected along marsh bank");
        dto.setLocation("200 Feet Radial Road, near Pillar #18");
        dto.setZone("Zone A - Radial Road Corridor");
        dto.setSeverity("HIGH");
        dto.setReporterName("Test Citizen");
        dto.setReporterEmail("test.citizen@example.com");
        dto.setReporterPhone("+91 98840 12345");

        // Validate DTO
        Set<ConstraintViolation<ReportRequestDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid DTO should have no constraint violations");

        // Execute controller method
        ResponseEntity<ApiResponseDto<WasteReport>> response = reportController.submitReportJson(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        WasteReport created = response.getBody().getData();
        assertNotNull(created.getId());
        assertEquals("Plastic Waste", created.getCategory());
        assertEquals("SUBMITTED", created.getStatus());

        // Verify entity is stored in database
        assertTrue(reportRepository.existsById(created.getId()), "Report entity should exist in repository");
    }

    @Test
    @DisplayName("POST /api/reports (Multipart) - Valid report with photo attachment saves and assigns photo URL")
    void testSubmitReportWithPhotoSuccess() {
        MockMultipartFile photo = new MockMultipartFile(
                "photo",
                "test-dumping.jpg",
                "image/jpeg",
                "fake-photo-image-bytes".getBytes()
        );

        ResponseEntity<ApiResponseDto<WasteReport>> response = reportController.submitReportWithPhoto(
                "Plastic Waste",
                "Large discarded plastic containers observed blocking freshwater culvert",
                "Velachery MRTS Terminal southern edge",
                "Zone B - Velachery MRTS Edge",
                "HIGH",
                "Eco Volunteer",
                "volunteer@ecoguard.org",
                "+91 98400 99999",
                photo
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());

        WasteReport created = response.getBody().getData();
        assertNotNull(created.getId());
        assertNotNull(created.getImageUrl(), "Image URL must be populated when photo is attached");
        assertTrue(created.getImageUrl().startsWith("/uploads/photos/"), "Image URL should point to uploads directory");
        assertTrue(reportRepository.existsById(created.getId()));
    }

    @Test
    @DisplayName("POST /api/reports (Multipart) - Reject invalid photo file extension")
    void testSubmitReportWithInvalidPhotoExtension() {
        MockMultipartFile invalidPhoto = new MockMultipartFile(
                "photo",
                "malicious.exe",
                "application/octet-stream",
                "executable-binary-content".getBytes()
        );

        assertThrows(IllegalArgumentException.class, () -> {
            reportController.submitReportWithPhoto(
                    "Plastic Waste",
                    "Description with over 10 valid characters",
                    "Pallikaranai Location",
                    "Zone A",
                    "LOW",
                    "Reporter",
                    "rep@example.com",
                    null,
                    invalidPhoto
            );
        });
    }

    @Test
    @DisplayName("DTO Validation - Rejects report when description is under 10 characters")
    void testValidationFailureShortDescription() {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setCategory("Plastic Waste");
        dto.setDescription("Too short"); // 9 chars
        dto.setLocation("200 Feet Radial Road");

        Set<ConstraintViolation<ReportRequestDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "DTO with short description must produce validation violations");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("description")));
    }

    @Test
    @DisplayName("DTO Validation - Rejects report when category or location is blank")
    void testValidationFailureBlankFields() {
        ReportRequestDto dto = new ReportRequestDto();
        dto.setDescription("Valid description of over 10 characters length");

        Set<ConstraintViolation<ReportRequestDto>> violations = validator.validate(dto);
        assertEquals(2, violations.size(), "Missing category and location must both produce violations");
    }

    @Test
    @DisplayName("GET /api/reports - Retrieves list of saved reports with 200 OK")
    void testGetReports() {
        ResponseEntity<ApiResponseDto<List<WasteReport>>> response = reportController.getReports(null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getData());
        assertFalse(response.getBody().getData().isEmpty(), "Reports list should contain seeded reports");
    }

    @Test
    @DisplayName("GET /api/reports/{id} - Existing report returns 200 OK")
    void testGetReportByIdSuccess() {
        WasteReport firstReport = reportRepository.findAll().get(0);
        ResponseEntity<ApiResponseDto<WasteReport>> response = reportController.getReportById(firstReport.getId());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(firstReport.getId(), response.getBody().getData().getId());
    }

    @Test
    @DisplayName("GET /api/reports/{id} - Non-existing report returns 404 NOT FOUND")
    void testGetReportByIdNotFound() {
        ResponseEntity<ApiResponseDto<WasteReport>> response = reportController.getReportById(999999L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status - Updates report status and records audit history trail")
    void testUpdateReportStatusSuccess() {
        // Create an initial report to update
        ReportRequestDto createDto = new ReportRequestDto();
        createDto.setCategory("Industrial Runoff");
        createDto.setDescription("Chemical discharge detected near Velachery link spillway");
        createDto.setLocation("Spillway bridge section 4");
        createDto.setZone("Zone E - Sholinganallur Radial Link");
        createDto.setSeverity("CRITICAL");

        ResponseEntity<ApiResponseDto<WasteReport>> createRes = reportController.submitReportJson(createDto);
        Long reportId = createRes.getBody().getData().getId();

        // Update status to IN_PROGRESS
        StatusUpdateRequestDto updateDto = new StatusUpdateRequestDto();
        updateDto.setStatus("IN_PROGRESS");
        updateDto.setUpdatedBy("Wetland Officer Rajan");
        updateDto.setComment("Dispatched JCB team for containment");
        updateDto.setUserRole("ADMIN");

        ResponseEntity<ApiResponseDto<WasteReport>> updateRes = reportController.updateStatus(reportId, updateDto);

        assertEquals(HttpStatus.OK, updateRes.getStatusCode());
        assertNotNull(updateRes.getBody());
        assertTrue(updateRes.getBody().isSuccess());
        assertEquals("IN_PROGRESS", updateRes.getBody().getData().getStatus());

        // Verify status in DB
        WasteReport updatedFromDb = reportRepository.findById(reportId).orElseThrow();
        assertEquals("IN_PROGRESS", updatedFromDb.getStatus());

        // Verify audit history trail was written
        ResponseEntity<ApiResponseDto<List<StatusHistory>>> historyRes = reportController.getReportHistory(reportId);
        assertEquals(HttpStatus.OK, historyRes.getStatusCode());
        List<StatusHistory> historyList = historyRes.getBody().getData();
        assertNotNull(historyList);
        assertTrue(historyList.size() >= 2, "History trail should contain initial submission and update");
        assertTrue(historyList.stream().anyMatch(h -> "IN_PROGRESS".equals(h.getNewStatus())));
    }

    @Test
    @DisplayName("PATCH /api/reports/{id}/status - Non-existing report ID returns 404 NOT FOUND")
    void testUpdateReportStatusNotFound() {
        StatusUpdateRequestDto updateDto = new StatusUpdateRequestDto();
        updateDto.setStatus("RESOLVED");
        updateDto.setUpdatedBy("Admin");
        updateDto.setComment("Cleaned up");

        ResponseEntity<ApiResponseDto<WasteReport>> response = reportController.updateStatus(999999L, updateDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("StatusUpdateRequestDto Validation - Rejects invalid status string")
    void testStatusUpdateRequestDtoValidation() {
        StatusUpdateRequestDto updateDto = new StatusUpdateRequestDto();
        updateDto.setStatus("NOT_A_VALID_STATUS");

        Set<ConstraintViolation<StatusUpdateRequestDto>> violations = validator.validate(updateDto);
        assertFalse(violations.isEmpty(), "Invalid status must trigger validation error");
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("status")));
    }
}
