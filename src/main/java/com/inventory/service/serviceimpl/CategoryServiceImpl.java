package com.inventory.service.serviceimpl;

import com.inventory.service.dao.api.CategoryRepository;
import com.inventory.service.model.entity.Category;
import com.inventory.service.model.entity.vo.CategoryVo;
import com.inventory.service.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryVo createCategory(CategoryVo categoryVo) {

        Category category = new Category();

        category.setCategoryName(categoryVo.getCategoryName());
        category.setDescription(categoryVo.getDescription());

        Category savedCategory = categoryRepository.save(category);

        return convertToVo(savedCategory);
    }

    @Override
    public CategoryVo updateCategory(Long categoryId, CategoryVo categoryVo) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setCategoryName(categoryVo.getCategoryName());
        category.setDescription(categoryVo.getDescription());

        Category updatedCategory = categoryRepository.save(category);

        return convertToVo(updatedCategory);
    }

    @Override
    public CategoryVo getCategoryById(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        return convertToVo(category);
    }

    @Override
    public List<CategoryVo> getAllCategories() {

        return categoryRepository.findAll()
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public void deleteCategory(Long categoryId) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        categoryRepository.delete(category);
    }

    private CategoryVo convertToVo(Category category) {

        return CategoryVo.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .build();
    }
}