package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "Operational status endpoints")
public class HealthController {

    @GetMapping(ApiPaths.HEALTH)
    @Operation(summary = "Check API health", responses = {
            @ApiResponse(responseCode = "200", description = "Server is running")
    })
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
