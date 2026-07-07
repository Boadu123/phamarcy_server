package com.example.phamarcy_server.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PharmacyDashboardResponse(
        UUID pharmacyId,
        String pharmacyName,
        String location,
        long totalInventoryRecords,
        BigDecimal totalInventoryValue,
        long totalSalesCount,
        BigDecimal totalSalesAmount
) {
}