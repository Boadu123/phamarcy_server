package com.example.phamarcy_server.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventoryResponse(
        UUID id,
        UUID pharmacyId,
        String productName,
        Integer quantity,
        BigDecimal price,
        Instant lastUpdatedAt
) {
}