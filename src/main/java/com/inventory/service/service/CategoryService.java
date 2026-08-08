package com.inventory.service.service;

import com.inventory.service.model.entity.vo.CategoryVo;

import java.util.List;

public interface CategoryService {

    CategoryVo createCategory(CategoryVo categoryVo);

    CategoryVo updateCategory(Long categoryId, CategoryVo categoryVo);

    CategoryVo getCategoryById(Long categoryId);

    List<CategoryVo> getAllCategories();

    void deleteCategory(Long categoryId);
}