package com.ecoguard.repository;

import com.ecoguard.model.WasteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WasteCategoryRepository extends JpaRepository<WasteCategory, Long> {
    Optional<WasteCategory> findByCode(String code);
    Optional<WasteCategory> findByName(String name);
}
