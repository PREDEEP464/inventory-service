package com.inventory.service.serviceimpl;
import com.inventory.service.model.entity.vo.InventoryStatisticsVo;

import com.inventory.service.dao.api.CategoryRepository;
import com.inventory.service.dao.api.ProductRepository;
import com.inventory.service.model.entity.Category;
import com.inventory.service.model.entity.Product;
import com.inventory.service.model.entity.vo.ProductVo;
import com.inventory.service.service.ProductService;
import com.inventory.service.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.inventory.service.cache.ProductCache;

import java.util.ArrayList;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductCache productCache;

    public ProductServiceImpl(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            ProductCache productCache) {

        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCache = productCache;
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

        ProductVo createdProduct = convertToVo(savedProduct);

        productCache.put(createdProduct);

        return createdProduct;
    }

    @Override
    public ProductVo updateProduct(Long productId, ProductVo productVo) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Category category = categoryRepository.findById(productVo.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        product.setProductCode(productVo.getProductCode());
        product.setProductName(productVo.getProductName());
        product.setProductDescription(productVo.getProductDescription());
        product.setCategory(category);
        product.setProductPrice(productVo.getProductPrice());
        product.setTotalQuantity(productVo.getTotalQuantity());
        product.setAvailableQuantity(productVo.getAvailableQuantity());
        product.setIsActive(productVo.getIsActive());

        Product updatedProduct = productRepository.save(product);

        ProductVo updatedProductVo = convertToVo(updatedProduct);

        productCache.put(updatedProductVo);

        return updatedProductVo;
    }

    @Override
    public ProductVo getProductById(Long productId) {

        // First check cache
        ProductVo cachedProduct = productCache.get(productId);

        if (cachedProduct != null) {
            return cachedProduct;
        }

        // Cache miss → fetch from database
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductVo productVo = convertToVo(product);

        // Store in cache
        productCache.put(productVo);

        return productVo;
    }

    @Override
    public List<ProductVo> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public void deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);

        productCache.remove(productId);
    }

    @Override
    public List<ProductVo> getProductsByCategory(Long categoryId) {

        return productRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByIsActive(Boolean isActive) {

        return productRepository.findByIsActive(isActive)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByPriceRange(
            BigDecimal minPrice,
            BigDecimal maxPrice) {

        return productRepository
                .findByProductPriceBetween(minPrice, maxPrice)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> searchProductsByName(String name) {

        return productRepository
                .findByProductNameContainingIgnoreCase(name)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public Page<ProductVo> filterProducts(
            Long categoryId,
            Boolean isActive,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable) {

        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.filterProducts(
                        categoryId,
                        isActive,
                        minPrice,
                        maxPrice,
                        name
                ),
                pageable
        );

        return productPage.map(this::convertToVo);
    }

    @Override
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

        ProductVo updatedProductVo = convertToVo(updatedProduct);

        productCache.put(updatedProductVo);

        return updatedProductVo;
    }

    @Override
    public ProductVo reduceStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (quantity > product.getAvailableQuantity()) {
            throw new RuntimeException("Insufficient stock");
        }

        product.setAvailableQuantity(
                product.getAvailableQuantity() - quantity
        );

        Product updatedProduct = productRepository.save(product);

        ProductVo updatedProductVo = convertToVo(updatedProduct);

        productCache.put(updatedProductVo);

        return updatedProductVo;
    }

    @Override
    public ProductVo restoreStock(Long productId, Integer quantity) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setAvailableQuantity(
                product.getAvailableQuantity() + quantity
        );

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

        // Add every created product to cache
        createdProducts.forEach(productCache::put);

        return createdProducts;
    }

    @Override
    public void deleteProducts(List<Long> productIds) {

        if (productIds == null || productIds.isEmpty()) {
            throw new RuntimeException("Product IDs cannot be empty");
        }

        List<Product> products =
                productRepository.findAllById(productIds);

        if (products.size() != productIds.size()) {
            throw new RuntimeException("One or more products not found");
        }

        productRepository.deleteAll(products);

        // Remove deleted products from cache
        productIds.forEach(productCache::remove);
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