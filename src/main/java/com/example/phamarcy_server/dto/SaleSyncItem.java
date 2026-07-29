package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaleSyncItem(
        @NotNull
        UUID id,
        @JsonProperty("user_id")
        @NotNull
        UUID userId,
        @JsonProperty("sale_date")
        @NotNull
        Instant saleDate,
        @JsonProperty("total_amount")
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal totalAmount,
        @JsonProperty("sync_status")
        @NotNull
        SyncStatus syncStatus,
        @JsonProperty("last_updated_at")
        @NotNull
        Instant lastUpdatedAt
) {
}
