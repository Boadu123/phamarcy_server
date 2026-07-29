package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PharmacyDashboardResponse(
        UUID pharmacyId,
        String pharmacyName,
        String location,
        SyncActivityStatus syncStatus,
        Instant lastSyncAt,
        long totalInventoryRecords,
        long totalUnitsInStock,
        BigDecimal totalInventoryValue,
        long totalSalesCount,
        BigDecimal totalSalesAmount,
        long successfulSyncs,
        long failedSyncs
) {
}
