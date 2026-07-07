package com.example.phamarcy_server.dto;

public record SyncEntityResult(
        int inserted,
        int updated,
        int ignored
) {
}