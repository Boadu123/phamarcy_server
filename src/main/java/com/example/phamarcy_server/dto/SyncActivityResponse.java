package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "One synchronization attempt with frontend-ready status and record counts")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SyncActivityResponse(
        UUID id,
        UUID pharmacyId,
        String pharmacyName,
        @Schema(allowableValues = {"IN_PROGRESS", "SUCCESSFUL", "FAILED"}) SyncActivityStatus status,
        Instant startedAt,
        Instant completedAt,
        Long durationMs,
        int recordsReceived,
        int recordsInserted,
        int recordsUpdated,
        int recordsIgnored,
        int inventoryRecordsReceived,
        int inventoryRecordsApplied,
        int salesRecordsReceived,
        int salesRecordsApplied,
        String message
) {
}
