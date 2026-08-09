package com.inventory.service.cache;

import com.inventory.service.model.entity.vo.ProductVo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProductCache {

    private final Map<Long, ProductVo> cache = new HashMap<>();

    public synchronized ProductVo get(Long productId) {

        ProductVo product = cache.get(productId);

        if (product != null) {
            System.out.println("CACHE HIT - Product ID: " + productId);
        } else {
            System.out.println("CACHE MISS - Product ID: " + productId);
        }

        return product;
    }

    public synchronized void put(ProductVo product) {

        cache.put(product.getProductId(), product);

        System.out.println(
                "CACHE UPDATED - Product ID: " + product.getProductId()
        );
    }

    public synchronized void remove(Long productId) {

        cache.remove(productId);

        System.out.println(
                "CACHE REMOVED - Product ID: " + productId
        );
    }

    public synchronized void clear() {

        cache.clear();

        System.out.println("CACHE CLEARED");
    }
}