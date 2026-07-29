package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductSyncItem(
        @NotNull
        UUID id,
        @NotBlank
        @Size(max = 255)
        String name,
        String description,
        @Size(max = 120)
        String category,
        @JsonProperty("reorder_level")
        @NotNull
        @Min(0)
        Integer reorderLevel,
        @JsonProperty("created_at")
        @NotNull
        Instant createdAt,
        @JsonProperty("sync_status")
        @NotNull
        SyncStatus syncStatus,
        @JsonProperty("last_updated_at")
        @NotNull
        Instant lastUpdatedAt
) {
}
