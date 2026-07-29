package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaleResponse(
        UUID id,
        UUID pharmacyId,
        UUID userId,
        String username,
        BigDecimal totalAmount,
        Instant saleDate,
        int itemCount,
        Instant lastUpdatedAt
) {
}
