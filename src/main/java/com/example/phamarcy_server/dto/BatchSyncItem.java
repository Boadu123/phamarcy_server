package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BatchSyncItem(
        @NotNull
        UUID id,
        @JsonProperty("product_id")
        @NotNull
        UUID productId,
        @JsonProperty("stock_reference")
        @NotBlank
        @Size(max = 64)
        String stockReference,
        @JsonProperty("batch_number")
        @NotBlank
        @Size(max = 120)
        String batchNumber,
        @NotNull
        @Min(0)
        Integer quantity,
        @JsonProperty("cost_price")
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal costPrice,
        @JsonProperty("selling_price")
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal sellingPrice,
        @JsonProperty("expiry_date")
        LocalDate expiryDate,
        @JsonProperty("sync_status")
        @NotNull
        SyncStatus syncStatus,
        @JsonProperty("last_updated_at")
        @NotNull
        Instant lastUpdatedAt
) {
}
