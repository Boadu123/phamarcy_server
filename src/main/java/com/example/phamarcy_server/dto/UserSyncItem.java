package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserSyncItem(
        @NotNull
        UUID id,
        @NotBlank
        @Size(max = 100)
        String username,
        @JsonProperty("password_hash")
        @NotBlank
        @Size(max = 255)
        String passwordHash,
        @NotBlank
        @Size(max = 50)
        String role,
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
