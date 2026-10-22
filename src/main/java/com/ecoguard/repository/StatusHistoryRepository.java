package com.ecoguard.repository;

import com.ecoguard.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByReportIdOrderByUpdatedAtDesc(Long reportId);
    void deleteByReportId(Long reportId);
}
