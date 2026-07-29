package com.example.phamarcy_server.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SalesStatisticsResponse(
        long totalTransactions,
        BigDecimal totalAmount
) {
}
