package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "One active line item belonging to a sale")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SaleItemResponse(
        UUID id,
        UUID productId,
        String productName,
        UUID batchId,
        String stockReference,
        String batchNumber,
        Integer quantitySold,
        BigDecimal unitPrice,
        BigDecimal subtotal,
        Instant createdAt,
        Instant lastUpdatedAt
) {
}
