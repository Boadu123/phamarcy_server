package com.example.phamarcy_server.dto;

public record ApiViolation(
        String field,
        String message
) {
}