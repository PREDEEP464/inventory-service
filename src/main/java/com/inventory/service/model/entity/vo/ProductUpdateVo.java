package com.inventory.service.model.entity.vo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateVo {

    private Long productId;

    @Size(max = 50, message = "Product code must not exceed 50 characters")
    private String productCode;

    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @Size(max = 500, message = "Product description must not exceed 500 characters")
    private String productDescription;

    private Long categoryId;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Product price must be greater than 0")
    private BigDecimal productPrice;

    private Boolean isActive;
}
