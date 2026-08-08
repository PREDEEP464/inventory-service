package com.inventory.service.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

import com.inventory.service.model.entity.vo.ProductVo;
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
    public ResponseEntity<ProductVo> createProduct(
            @Valid @RequestBody ProductVo productVo) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(productService.createProduct(productVo));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductVo> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductVo productVo) {

        return ResponseEntity.ok(
                productService.updateProduct(productId, productVo)
        );
    }

    // Not use anymore as this is already done inside the filter part
//    @GetMapping
//    public ResponseEntity<List<ProductVo>> getAllProducts() {
//
//        return ResponseEntity.ok(
//                productService.getAllProducts()
//        );
//    }

    @GetMapping
    public ResponseEntity<Page<ProductVo>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String name,
            Pageable pageable) {


        return ResponseEntity.ok(
                productService.filterProducts(
                        categoryId,
                        isActive,
                        minPrice,
                        maxPrice,
                        name,
                        pageable
                )
        );
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductVo> getProductsByCategory(@PathVariable Long categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/active/{isActive}")
    public List<ProductVo> getProductsByIsActive(
            @PathVariable Boolean isActive) {

        return productService.getProductsByIsActive(isActive);
    }

    @GetMapping("/price-range")
    public List<ProductVo> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        return productService.getProductsByPriceRange(minPrice, maxPrice);
    }

    @GetMapping("/search")
    public List<ProductVo> searchProductsByName(
            @RequestParam String name) {

        return productService.searchProductsByName(name);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductVo> getProductById(
            @PathVariable Long productId) {

        return ResponseEntity.ok(
                productService.getProductById(productId)
        );
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId) {

        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }
}