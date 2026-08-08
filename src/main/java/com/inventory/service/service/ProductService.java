package com.inventory.service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.service.model.entity.vo.ProductVo;
import java.math.BigDecimal;

import java.util.List;

public interface ProductService {

    ProductVo createProduct(ProductVo productVo);

    ProductVo updateProduct(Long productId, ProductVo productVo);

    ProductVo getProductById(Long productId);

    List<ProductVo> getAllProducts();

    List<ProductVo> getProductsByCategory(Long categoryId);

    List<ProductVo> getProductsByStatus(String status);

    List<ProductVo> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<ProductVo> searchProductsByName(String name);

    Page<ProductVo> filterProducts(
            Long categoryId,
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable
    );

    void deleteProduct(Long productId);
}