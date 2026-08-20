package com.inventory.service.serviceimpl;
import com.inventory.service.model.entity.vo.InventoryStatisticsVo;
import org.springframework.transaction.annotation.Transactional;

import com.inventory.service.dao.api.CategoryRepository;
import com.inventory.service.dao.api.ProductRepository;
import com.inventory.service.model.entity.Category;
import com.inventory.service.model.entity.Product;
import com.inventory.service.model.entity.vo.ProductVo;
import com.inventory.service.model.entity.vo.ProductUpdateVo;
import com.inventory.service.service.ProductService;
import com.inventory.service.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public ProductVo createProduct(ProductVo productVo) {

        Category category = categoryRepository.findById(productVo.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Product product = new Product();

        product.setProductCode(productVo.getProductCode());
        product.setProductName(productVo.getProductName());
        product.setProductDescription(productVo.getProductDescription());
        product.setCategory(category);
        product.setProductPrice(productVo.getProductPrice());
        product.setTotalQuantity(productVo.getTotalQuantity());
        product.setAvailableQuantity(productVo.getAvailableQuantity());
        product.setIsActive(productVo.getIsActive());

        Product savedProduct = productRepository.save(product);

        return convertToVo(savedProduct);
    }

    @Override
    @CacheEvict(
            cacheNames = "products",
            allEntries = true
    )
    @Transactional
    public List<ProductVo> updateProducts(
            List<ProductUpdateVo> productUpdates) {

        List<ProductVo> updatedProducts = new ArrayList<>();

        for (ProductUpdateVo updateVo : productUpdates) {

            Product product = productRepository.findById(updateVo.getProductId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Product not found: " + updateVo.getProductId()
                            ));

            if (updateVo.getProductCode() != null) {
                product.setProductCode(updateVo.getProductCode());
            }

            if (updateVo.getProductName() != null) {
                product.setProductName(updateVo.getProductName());
            }

            if (updateVo.getProductDescription() != null) {
                product.setProductDescription(
                        updateVo.getProductDescription()
                );
            }

            if (updateVo.getCategoryId() != null) {

                Category category = categoryRepository
                        .findById(updateVo.getCategoryId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found: "
                                                + updateVo.getCategoryId()
                                ));

                product.setCategory(category);
            }

            if (updateVo.getProductPrice() != null) {
                product.setProductPrice(updateVo.getProductPrice());
            }

            if (updateVo.getIsActive() != null) {
                product.setIsActive(updateVo.getIsActive());
            }

            Product updatedProduct =
                    productRepository.save(product);

            updatedProducts.add(
                    convertToVo(updatedProduct)
            );
        }

        return updatedProducts;
    }

    @Override
    @Cacheable(
            cacheNames = "products",
            key = "#productId"
    )
    public ProductVo getProductById(Long productId) {

        Product product = productRepository
                .findByProductIdAndIsActive(productId, true)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return convertToVo(product);
    }

    @Override
    public List<ProductVo> getAllProducts() {

        return productRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByCategory(Long categoryId) {

        return productRepository
                .findByCategoryCategoryIdAndIsActiveTrue(categoryId)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByIsActive(Boolean isActive) {

        return productRepository
                .findByIsActive(isActive)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository
                .findByProductPriceBetweenAndIsActiveTrue(
                        minPrice,
                        maxPrice
                )
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> searchProductsByName(String name) {

        return productRepository
                .findByProductNameContainingIgnoreCaseAndIsActiveTrue(name)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public Page<ProductVo> filterProducts(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable) {

        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.filterProducts(
                        categoryId,
                        minPrice,
                        maxPrice,
                        name
                ),
                pageable
        );

        return productPage.map(this::convertToVo);
    }

    @Override
    @CacheEvict(
            cacheNames = "products",
            key = "#productId"
    )
    public ProductVo updateStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setTotalQuantity(
                product.getTotalQuantity() + quantity
        );

        product.setAvailableQuantity(
                product.getAvailableQuantity() + quantity
        );

        Product updatedProduct = productRepository.save(product);

        return convertToVo(updatedProduct);
    }

    @Transactional
    @Override
    @CacheEvict(
            cacheNames = "products",
            key = "#productId"
    )
    public ProductVo reduceStock(Long productId, Integer quantity) {

        int updatedRows = productRepository.reduceStock(
                productId,
                quantity
        );

        if (updatedRows == 0) {

            Product product = productRepository.findById(productId)
                    .orElseThrow(() ->
                            new RuntimeException("Product not found"));

            throw new RuntimeException("Insufficient stock");
        }

        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return convertToVo(updatedProduct);
    }

    @Transactional
    @Override
    @CacheEvict(
            cacheNames = "products",
            key = "#productId"
    )
    public ProductVo restoreStock(Long productId, Integer quantity) {

        int updatedRows = productRepository.restoreStock(
                productId,
                quantity
        );

        if (updatedRows == 0) {
            throw new RuntimeException("Product not found");
        }

        Product updatedProduct = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found"));

        return convertToVo(updatedProduct);
    }

    @Override
    @CacheEvict(
            cacheNames = "products",
            key = "#productId"
    )
    public ProductVo updateProductStatus(Long productId, Boolean isActive) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException("Product not found with ID: " + productId));

        product.setIsActive(isActive);

        Product updatedProduct = productRepository.save(product);

        return convertToVo(updatedProduct);
    }

    @Override
    public List<ProductVo> getLowStockProducts(Integer threshold) {

        if (threshold == null || threshold <= 0) {
            throw new IllegalArgumentException(
                    "Low stock threshold must be greater than 0"
            );
        }

        return productRepository
                .findByAvailableQuantityLessThanEqualAndIsActive(
                        threshold,
                        true
                )
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public InventoryStatisticsVo getInventoryStatistics() {

        List<Product> products = productRepository.findAll();

        long totalProducts = products.size();

        long activeProducts = products.stream()
                .filter(product -> Boolean.TRUE.equals(product.getIsActive()))
                .count();

        long inactiveProducts = products.stream()
                .filter(product -> Boolean.FALSE.equals(product.getIsActive()))
                .count();

        long totalStock = products.stream()
                .mapToLong(Product::getTotalQuantity)
                .sum();

        long availableStock = products.stream()
                .mapToLong(Product::getAvailableQuantity)
                .sum();

        long lowStockProducts = products.stream()
                .filter(product -> product.getAvailableQuantity() <= 10)
                .count();

        return InventoryStatisticsVo.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .inactiveProducts(inactiveProducts)
                .totalStock(totalStock)
                .availableStock(availableStock)
                .lowStockProducts(lowStockProducts)
                .build();
    }

    @Override
    public List<ProductVo> createProducts(List<ProductVo> productVos) {

        List<Product> products = new ArrayList<>();

        for (ProductVo productVo : productVos) {

            Category category = categoryRepository
                    .findById(productVo.getCategoryId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found: "
                                            + productVo.getCategoryId()
                            )
                    );

            Product product = new Product();

            product.setProductCode(productVo.getProductCode());
            product.setProductName(productVo.getProductName());
            product.setProductDescription(productVo.getProductDescription());
            product.setCategory(category);
            product.setProductPrice(productVo.getProductPrice());
            product.setTotalQuantity(productVo.getTotalQuantity());
            product.setAvailableQuantity(productVo.getAvailableQuantity());
            product.setIsActive(productVo.getIsActive());

            products.add(product);
        }

        List<Product> savedProducts =
                productRepository.saveAll(products);

        List<ProductVo> createdProducts = savedProducts
                .stream()
                .map(this::convertToVo)
                .toList();

        return createdProducts;
    }

    private ProductVo convertToVo(Product product) {

        return ProductVo.builder()
                .productId(product.getProductId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .productDescription(product.getProductDescription())
                .categoryId(product.getCategory().getCategoryId())
                .productPrice(product.getProductPrice())
                .totalQuantity(product.getTotalQuantity())
                .availableQuantity(product.getAvailableQuantity())
                .isActive(product.getIsActive())
                .build();
    }
}