package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Complete details for one sale within a pharmacy")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaleDetailsResponse(
        UUID id,
        UUID pharmacyId,
        String pharmacyName,
        String location,
        UUID userId,
        String username,
        BigDecimal totalAmount,
        Instant saleDate,
        int itemCount,
        Instant createdAt,
        Instant lastUpdatedAt,
        List<SaleItemResponse> items
) {
    public SaleDetailsResponse {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
