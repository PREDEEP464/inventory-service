package com.inventory.service.dao.api;

import com.inventory.service.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.math.BigDecimal;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product>{

    List<Product> findByCategoryCategoryId(Long categoryId);

    List<Product> findByStatus(String status);

    List<Product> findByProductPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<Product> findByProductNameContainingIgnoreCase(String productName);
}