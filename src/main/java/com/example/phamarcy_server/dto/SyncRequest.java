package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SyncRequest(
        @JsonProperty("pharmacyId")
        @JsonAlias("pharmacy_id")
        @NotNull
        UUID pharmacyId,
        @Valid
        SyncRecords records
) {
    public SyncRequest {
        records = records == null ? SyncRecords.empty() : records;
    }
}
