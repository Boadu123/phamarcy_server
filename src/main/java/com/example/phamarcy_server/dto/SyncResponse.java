package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SyncResponse(
        UUID pharmacyId,
        SyncEntityResult users,
        SyncEntityResult products,
        SyncEntityResult batches,
        SyncEntityResult sales,
        SyncEntityResult saleItems,
        SyncEntityResult appSettings
) {
}
