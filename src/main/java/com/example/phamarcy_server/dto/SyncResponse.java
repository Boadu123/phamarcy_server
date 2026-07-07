package com.example.phamarcy_server.dto;

import java.util.UUID;

public record SyncResponse(
        UUID pharmacyId,
        SyncEntityResult inventory,
        SyncEntityResult sales
) {
}