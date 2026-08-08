package com.inventory.service.controller;

import com.inventory.service.model.entity.vo.CategoryVo;
import com.inventory.service.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryVo categoryVo) {

        CategoryVo createdCategory =
                categoryService.createCategory(categoryVo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        Map.of(
                                "message", "Category created successfully",
                                "data", createdCategory
                        )
                );
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryVo categoryVo) {

        CategoryVo updatedCategory =
                categoryService.updateCategory(categoryId, categoryVo);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Category updated successfully",
                        "data", updatedCategory
                )
        );
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryVo> getCategoryById(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                categoryService.getCategoryById(categoryId)
        );
    }

    @GetMapping
    public ResponseEntity<List<CategoryVo>> getAllCategories() {

        return ResponseEntity.ok(
                categoryService.getAllCategories()
        );
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Map<String, String>> deleteCategory(
            @PathVariable Long categoryId) {

        categoryService.deleteCategory(categoryId);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Category deleted successfully"
                )
        );
    }
}