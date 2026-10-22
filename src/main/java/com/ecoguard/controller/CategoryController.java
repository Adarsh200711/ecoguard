package com.ecoguard.controller;

import com.ecoguard.dto.ApiResponseDto;
import com.ecoguard.model.WasteCategory;
import com.ecoguard.repository.WasteCategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    private final WasteCategoryRepository categoryRepository;

    public CategoryController(WasteCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<WasteCategory>>> getCategories() {
        List<WasteCategory> categories = categoryRepository.findAll();
        return ResponseEntity.ok(ApiResponseDto.success("Categories retrieved", categories));
    }
}
