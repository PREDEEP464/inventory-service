package com.inventory.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import jakarta.validation.constraints.Min;

import com.inventory.service.model.entity.vo.ProductVo;
import com.inventory.service.model.entity.vo.ProductUpdateVo;
import com.inventory.service.model.entity.vo.StockUpdateVo;
import com.inventory.service.model.entity.vo.ApiResponse;
import com.inventory.service.model.entity.vo.InventoryStatisticsVo;
import com.inventory.service.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductVo>> createProduct(
            @Valid @RequestBody ProductVo productVo) {

        ProductVo createdProduct = productService.createProduct(productVo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "Product created successfully",
                                createdProduct
                        )
                );
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<List<ProductVo>>> updateProducts(
            @Valid @RequestBody List<ProductUpdateVo> productUpdates) {

        List<ProductVo> updatedProducts =
                productService.updateProducts(productUpdates);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products updated successfully",
                        updatedProducts
                )
        );
    }

    // Filter + Pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductVo>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Page<ProductVo> products = productService.filterProducts(
                categoryId,
                minPrice,
                maxPrice,
                name,
                pageable
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        products
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductVo>>> getProductsByCategory(
            @PathVariable Long categoryId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        productService.getProductsByCategory(categoryId)
                )
        );
    }

    @GetMapping("/active/{isActive}")
    public ResponseEntity<ApiResponse<List<ProductVo>>> getProductsByIsActive(
            @PathVariable Boolean isActive) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        productService.getProductsByIsActive(isActive)
                )
        );
    }

    @GetMapping("/price-range")
    public ResponseEntity<ApiResponse<List<ProductVo>>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        productService.getProductsByPriceRange(
                                minPrice,
                                maxPrice
                        )
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductVo>>> searchProductsByName(
            @RequestParam String name) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products fetched successfully",
                        productService.searchProductsByName(name)
                )
        );
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<ProductVo>>> createProducts(
            @Valid @RequestBody List<ProductVo> productVos) {

        List<ProductVo> createdProducts =
                productService.createProducts(productVos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        new ApiResponse<>(
                                "Products created successfully",
                                createdProducts
                        )
                );
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductVo>> getProductById(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product fetched successfully",
                        productService.getProductById(productId)
                )
        );
    }

    @PatchMapping("/re-stock")
    public ResponseEntity<ApiResponse<List<ProductVo>>> updateStock(
            @Valid @RequestBody List<StockUpdateVo> stockUpdates) {

        List<ProductVo> updatedProducts =
                productService.updateStock(stockUpdates);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products restocked successfully",
                        updatedProducts
                )
        );
    }

    @PatchMapping("/stock/reduce")
    public ResponseEntity<ApiResponse<List<ProductVo>>> reduceStock(
            @Valid @RequestBody List<StockUpdateVo> stockUpdates) {

        List<ProductVo> updatedProducts =
                productService.reduceStock(stockUpdates);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product stocks reduced successfully",
                        updatedProducts
                )
        );
    }

    @PatchMapping("/stock/restore")
    public ResponseEntity<ApiResponse<List<ProductVo>>> restoreStock(
            @Valid @RequestBody List<StockUpdateVo> stockUpdates) {

        List<ProductVo> updatedProducts =
                productService.restoreStock(stockUpdates);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product stocks restored successfully",
                        updatedProducts
                )
        );
    }

    @PatchMapping("/{productId}/status")
    public ResponseEntity<ApiResponse<ProductVo>> updateProductStatus(
            @PathVariable Long productId,
            @RequestParam Boolean isActive) {

        ProductVo updatedProduct =
                productService.updateProductStatus(productId, isActive);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product status updated successfully",
                        updatedProduct
                )
        );
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<ProductVo>>> getLowStockProducts(
            @RequestParam
            @Min(value = 1, message = "Threshold must be greater than 0")
            Integer threshold) {

        List<ProductVo> products =
                productService.getLowStockProducts(threshold);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Low stock products fetched successfully",
                        products
                )
        );
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<InventoryStatisticsVo>> getInventoryStatistics() {

        InventoryStatisticsVo statistics =
                productService.getInventoryStatistics();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Inventory statistics fetched successfully",
                        statistics
                )
        );
    }

}