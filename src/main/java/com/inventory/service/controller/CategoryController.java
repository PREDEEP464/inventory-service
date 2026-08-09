package com.inventory.service.controller;

import com.inventory.service.model.entity.vo.ApiResponse;
import com.inventory.service.model.entity.vo.CategoryVo;
import com.inventory.service.service.CategoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryVo>> createCategory(
            @Valid @RequestBody CategoryVo categoryVo) {

        CategoryVo createdCategory =
                categoryService.createCategory(categoryVo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "Category created successfully",
                                createdCategory
                        )
                );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryVo>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryVo categoryVo) {

        CategoryVo updatedCategory =
                categoryService.updateCategory(categoryId, categoryVo);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category updated successfully",
                        updatedCategory
                )
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<CategoryVo>> getCategoryById(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category fetched successfully",
                        categoryService.getCategoryById(categoryId)
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryVo>>> getAllCategories() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Categories fetched successfully",
                        categoryService.getAllCategories()
                )
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long categoryId) {

        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Category deleted successfully",
                        null
                )
        );
    }
}