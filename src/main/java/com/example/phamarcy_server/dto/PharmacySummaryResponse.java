package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Pharmacy list item; the UUID is an internal navigation identifier")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PharmacySummaryResponse(
        UUID pharmacyId,
        String pharmacyName,
        String location,
        SyncActivityStatus syncStatus,
        Instant lastSyncAt,
        long totalInventoryRecords,
        long totalUnitsInStock,
        BigDecimal totalInventoryValue,
        long totalSalesCount,
        BigDecimal totalSalesAmount
) {
}
