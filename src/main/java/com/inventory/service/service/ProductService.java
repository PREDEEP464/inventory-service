package com.inventory.service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.inventory.service.model.entity.vo.InventoryStatisticsVo;
import com.inventory.service.model.entity.vo.ProductVo;
import com.inventory.service.model.entity.vo.ProductUpdateVo;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductVo createProduct(ProductVo productVo);

    List<ProductVo> createProducts(List<ProductVo> productVos);

    List<ProductVo> updateProducts(List<ProductUpdateVo> productUpdates);

    ProductVo getProductById(Long productId);

    List<ProductVo> getAllProducts();

    List<ProductVo> getProductsByCategory(Long categoryId);

    List<ProductVo> getProductsByIsActive(Boolean isActive);

    ProductVo updateProductStatus(Long productId, Boolean isActive);

    List<ProductVo> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<ProductVo> searchProductsByName(String name);

    List<ProductVo> getLowStockProducts(Integer threshold);

    InventoryStatisticsVo getInventoryStatistics();

    ProductVo updateStock(Long productId, Integer quantity);

    ProductVo reduceStock(Long productId, Integer quantity);

    ProductVo restoreStock(Long productId, Integer quantity);

    Page<ProductVo> filterProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable
    );


}