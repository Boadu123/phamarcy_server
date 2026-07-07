package com.example.phamarcy_server.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SaleResponse(
        UUID id,
        UUID pharmacyId,
        BigDecimal totalAmount,
        Instant createdAt,
        Instant lastUpdatedAt
) {
}