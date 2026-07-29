package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record InventoryResponse(
        UUID id,
        UUID pharmacyId,
        UUID productId,
        String productName,
        String category,
        String stockReference,
        String batchNumber,
        Integer quantity,
        BigDecimal costPrice,
        BigDecimal sellingPrice,
        BigDecimal inventoryValue,
        LocalDate expiryDate,
        Instant lastUpdatedAt
) {
}
