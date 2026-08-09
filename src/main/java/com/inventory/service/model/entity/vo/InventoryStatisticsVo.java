package com.inventory.service.model.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryStatisticsVo {

    private long totalProducts;

    private long activeProducts;

    private long inactiveProducts;

    private long totalStock;

    private long availableStock;

    private long lowStockProducts;
}