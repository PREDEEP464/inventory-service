package com.inventory.service.controller;

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
    public ResponseEntity<CategoryVo> createCategory(
            @Valid @RequestBody CategoryVo categoryVo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(categoryService.createCategory(categoryVo));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryVo> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryVo categoryVo) {

        return ResponseEntity.ok(
                categoryService.updateCategory(categoryId, categoryVo)
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
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId) {

        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}