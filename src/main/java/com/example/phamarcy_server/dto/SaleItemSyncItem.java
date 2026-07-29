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
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SaleItemSyncItem(
        @NotNull
        UUID id,
        @JsonProperty("sale_id")
        @NotNull
        UUID saleId,
        @JsonProperty("batch_id")
        @NotNull
        UUID batchId,
        @JsonProperty("product_name")
        @NotBlank
        @Size(max = 255)
        String productName,
        @JsonProperty("batch_number")
        @NotBlank
        @Size(max = 120)
        String batchNumber,
        @JsonProperty("quantity_sold")
        @NotNull
        @Min(0)
        Integer quantitySold,
        @JsonProperty("unit_price")
        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 12, fraction = 2)
        BigDecimal unitPrice,
        @JsonProperty("sync_status")
        @NotNull
        SyncStatus syncStatus,
        @JsonProperty("last_updated_at")
        @NotNull
        Instant lastUpdatedAt
) {
}
