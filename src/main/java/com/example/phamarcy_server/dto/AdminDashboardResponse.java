package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AdminDashboardResponse(
        Instant generatedAt,
        long totalPharmacies,
        long pharmaciesWithSuccessfulSync,
        long totalInventoryRecords,
        long totalUnitsInStock,
        BigDecimal totalInventoryValue,
        long totalSalesCount,
        BigDecimal totalSalesAmount,
        long successfulSyncs,
        long failedSyncs,
        long inProgressSyncs,
        SyncActivityResponse latestSync,
        List<SyncActivityResponse> recentActivity
) {
}
