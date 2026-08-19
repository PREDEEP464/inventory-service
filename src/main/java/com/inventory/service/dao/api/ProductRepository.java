package com.inventory.service.dao.api;

import com.inventory.service.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product>{

    List<Product> findByCategoryCategoryId(Long categoryId);

    List<Product> findByIsActive(Boolean isActive);

    List<Product> findByProductPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<Product> findByProductNameContainingIgnoreCase(String productName);

    List<Product> findByAvailableQuantityLessThanEqualAndIsActive(
            Integer quantity,
            Boolean isActive
    );

    @Modifying
    @Query("""
            UPDATE Product p
            SET p.availableQuantity = p.availableQuantity - :quantity
            WHERE p.productId = :productId
              AND p.availableQuantity >= :quantity
            """)
    int reduceStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

    @Modifying
    @Query("""
            UPDATE Product p
            SET p.availableQuantity = p.availableQuantity + :quantity
            WHERE p.productId = :productId
            """)
    int restoreStock(
            @Param("productId") Long productId,
            @Param("quantity") Integer quantity
    );

}