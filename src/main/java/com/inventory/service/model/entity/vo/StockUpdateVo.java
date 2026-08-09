package com.inventory.service.model.entity.vo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateVo {

    @NotNull(message = "Stock quantity is required")
    @Min(value = 1, message = "Stock quantity must be greater than 0")
    private Integer quantity;
}