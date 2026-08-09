package com.inventory.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

import com.inventory.service.model.entity.vo.ProductVo;
import com.inventory.service.model.entity.vo.StockUpdateVo;
import com.inventory.service.model.entity.vo.ApiResponse;
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

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<ProductVo>> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVo productVo) {

        ProductVo updatedProduct =
                productService.updateProduct(productId, productVo);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product updated successfully",
                        updatedProduct
                )
        );
    }

    // Filter + Pagination
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ProductVo>>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            Pageable pageable) {

        Page<ProductVo> products = productService.filterProducts(
                categoryId,
                isActive,
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

    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> deleteProducts(
            @RequestBody List<Long> productIds) {

        productService.deleteProducts(productIds);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Products deleted successfully",
                        null
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

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<ProductVo>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateVo stockUpdateVo) {

        ProductVo updatedProduct = productService.updateStock(
                productId,
                stockUpdateVo.getQuantity()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product restocked successfully",
                        updatedProduct
                )
        );
    }

    @PatchMapping("/{productId}/stock/reduce")
    public ResponseEntity<ApiResponse> reduceStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateVo stockUpdateVo) {

        ProductVo updatedProduct = productService.reduceStock(
                productId,
                stockUpdateVo.getQuantity()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product stock reduced successfully",
                        updatedProduct
                )
        );
    }

    @PatchMapping("/{productId}/stock/restore")
    public ResponseEntity<ApiResponse<ProductVo>> restoreStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockUpdateVo stockUpdateVo) {

        ProductVo updatedProduct = productService.restoreStock(
                productId,
                stockUpdateVo.getQuantity()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product stock restored successfully",
                        updatedProduct
                )
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long productId) {

        productService.deleteProduct(productId);

        return ResponseEntity.ok(
                new ApiResponse<>(
                        "Product deleted successfully",
                        null
                )
        );
    }
}