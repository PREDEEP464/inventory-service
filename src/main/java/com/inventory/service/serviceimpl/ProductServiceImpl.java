package com.inventory.service.serviceimpl;

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
        product.setStatus(productVo.getStatus());

        Product savedProduct = productRepository.save(product);

        return convertToVo(savedProduct);
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
        product.setStatus(productVo.getStatus());

        Product updatedProduct = productRepository.save(product);

        return convertToVo(updatedProduct);
    }

    @Override
    public ProductVo getProductById(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return convertToVo(product);
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
    }

    @Override
    public List<ProductVo> getProductsByCategory(Long categoryId) {

        return productRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(this::convertToVo)
                .toList();
    }

    @Override
    public List<ProductVo> getProductsByStatus(String status) {

        return productRepository.findByStatus(status)
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
            String status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String name,
            Pageable pageable) {

        Page<Product> productPage = productRepository.findAll(
                ProductSpecification.filterProducts(
                        categoryId,
                        status,
                        minPrice,
                        maxPrice,
                        name
                ),
                pageable
        );

        return productPage.map(this::convertToVo);
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
                .status(product.getStatus())
                .build();
    }
}