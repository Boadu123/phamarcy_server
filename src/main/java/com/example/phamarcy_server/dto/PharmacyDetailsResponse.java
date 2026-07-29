package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Comprehensive pharmacy monitoring view")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PharmacyDetailsResponse(
        UUID pharmacyId,
        String pharmacyName,
        String location,
        SyncActivityStatus syncStatus,
        Instant lastSyncAt,
        InventoryStatisticsResponse inventory,
        SalesStatisticsResponse sales,
        long successfulSyncs,
        long failedSyncs,
        SyncActivityResponse latestSync,
        List<SyncActivityResponse> recentActivity
) {
}
