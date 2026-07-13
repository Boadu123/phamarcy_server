package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;
import com.example.phamarcy_server.service.SyncService;
import com.example.phamarcy_server.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Synchronization", description = "Offline-first client synchronization endpoints")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping(ApiPaths.SYNC)
    @Operation(
            summary = "Synchronize inventory and sales records",
            description = "Applies last-write-wins synchronization for a pharmacy branch using client-generated UUIDs.",
            security = @SecurityRequirement(name = "pharmacyToken"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Synchronization completed"),
                    @ApiResponse(responseCode = "401", description = "Invalid or missing pharmacy token"),
                    @ApiResponse(responseCode = "403", description = "Payload pharmacy_id does not match authenticated pharmacy"),
                    @ApiResponse(responseCode = "409", description = "Record ownership conflict detected")
            }
    )
    public ResponseEntity<SyncResponse> synchronize(
            @RequestAttribute(AuthenticatedPharmacy.REQUEST_ATTRIBUTE) AuthenticatedPharmacy authenticatedPharmacy,
            @Valid @RequestBody SyncRequest request
    ) {
        log.debug("Received synchronization request for pharmacy {}", request.pharmacyId());
        return ResponseEntity.ok(syncService.synchronize(request, authenticatedPharmacy));
    }
}