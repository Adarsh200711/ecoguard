package com.ecoguard.repository;

import com.ecoguard.model.WasteReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteReportRepository extends JpaRepository<WasteReport, Long> {

    List<WasteReport> findAllByOrderByCreatedAtDesc();

    List<WasteReport> findByStatusOrderByCreatedAtDesc(String status);

    List<WasteReport> findByCategoryOrderByCreatedAtDesc(String category);

    List<WasteReport> findByZoneOrderByCreatedAtDesc(String zone);

    long countByStatus(String status);

    long countByCategory(String category);

    @Query("SELECT r FROM WasteReport r WHERE " +
           "LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.location) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(r.zone) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY r.createdAt DESC")
    List<WasteReport> searchReports(@Param("query") String query);

    @Query("SELECT r FROM WasteReport r WHERE " +
           "(:status IS NULL OR :status = '' OR UPPER(:status) = 'ALL' OR UPPER(r.status) = UPPER(:status)) AND " +
           "(:category IS NULL OR :category = '' OR UPPER(:category) = 'ALL' OR LOWER(r.category) = LOWER(:category)) AND " +
           "(:zone IS NULL OR :zone = '' OR UPPER(:zone) = 'ALL' OR LOWER(r.zone) = LOWER(:zone)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(r.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.location) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.category) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.zone) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(r.reporterName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY r.createdAt DESC")
    List<WasteReport> filterReports(@Param("status") String status,
                                   @Param("category") String category,
                                   @Param("zone") String zone,
                                   @Param("search") String search);

    List<WasteReport> findByReporterEmailIgnoreCaseOrderByCreatedAtDesc(String reporterEmail);

    @Query("SELECT r.category, COUNT(r) FROM WasteReport r GROUP BY r.category")
    List<Object[]> countReportsByCategory();

    @Query("SELECT r.zone, COUNT(r) FROM WasteReport r WHERE r.zone IS NOT NULL GROUP BY r.zone")
    List<Object[]> countReportsByZone();

    @Query("SELECT r.status, COUNT(r) FROM WasteReport r GROUP BY r.status")
    List<Object[]> countReportsByStatus();
}
