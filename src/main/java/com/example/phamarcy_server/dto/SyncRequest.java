package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SyncRequest(
        @JsonProperty("pharmacy_id")
        @NotNull
        UUID pharmacyId,
        @Valid
        List<InventorySyncItem> inventory,
        @Valid
        List<SaleSyncItem> sales
) {
    public SyncRequest {
        inventory = inventory == null ? List.of() : List.copyOf(inventory);
        sales = sales == null ? List.of() : List.copyOf(sales);
    }
}