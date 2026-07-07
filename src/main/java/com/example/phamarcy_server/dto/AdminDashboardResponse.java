package com.example.phamarcy_server.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(
        long totalPharmacies,
        long totalInventoryRecords,
        BigDecimal totalInventoryValue,
        long totalSalesCount,
        BigDecimal totalSalesAmount
) {
}