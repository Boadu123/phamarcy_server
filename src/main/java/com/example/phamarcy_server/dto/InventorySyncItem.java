package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InventorySyncItem(
        @NotNull UUID id,
        @JsonProperty("product_name")
        @NotBlank
        @Size(max = 255)
        String productName,
        @NotNull
        @Min(0)
        Integer quantity,
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal price,
        @JsonProperty("last_updated_at")
        @NotNull
        Instant lastUpdatedAt
) {
}