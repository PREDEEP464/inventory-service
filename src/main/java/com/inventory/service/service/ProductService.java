package com.inventory.service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.service.model.entity.vo.ProductVo;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductVo createProduct(ProductVo productVo);

    List<ProductVo> createProducts(List<ProductVo> productVos);

    ProductVo updateProduct(Long productId, ProductVo productVo);

    ProductVo getProductById(Long productId);

    List<ProductVo> getAllProducts();

    List<ProductVo> getProductsByCategory(Long categoryId);

    List<ProductVo> getProductsByIsActive(Boolean isActive);

    List<ProductVo> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<ProductVo> searchProductsByName(String name);

    ProductVo updateStock(Long productId, Integer quantity);

    ProductVo reduceStock(Long productId, Integer quantity);

    Page<ProductVo> filterProducts(
            Long categoryId,
            Boolean isActive,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable
    );

    void deleteProduct(Long productId);

    void deleteProducts(List<Long> productIds);
}