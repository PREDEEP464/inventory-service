package com.inventory.service.dao.api;

import com.inventory.service.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository
        extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByProductIdAndIsActive(
            Long productId,
            Boolean isActive
    );

    List<Product> findByIsActiveTrueOrderByProductIdAsc();

    List<Product> findByCategoryCategoryIdAndIsActiveTrue(
            Long categoryId
    );

    List<Product> findByProductPriceBetweenAndIsActiveTrue(
            BigDecimal minPrice,
            BigDecimal maxPrice
    );

    List<Product> findByProductNameContainingIgnoreCaseAndIsActiveTrue(
            String name
    );

    List<Product> findByIsActive(Boolean isActive);

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