package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.ApiError;
import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.service.SyncActivityService;
import com.example.phamarcy_server.service.SyncService;
import com.example.phamarcy_server.util.ApiPaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Synchronization", description = "Offline-first client synchronization endpoints")
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final SyncService syncService;
    private final SyncActivityService syncActivityService;

    public SyncController(SyncService syncService, SyncActivityService syncActivityService) {
        this.syncService = syncService;
        this.syncActivityService = syncActivityService;
    }

    @PostMapping(ApiPaths.SYNC)
    @Operation(
            summary = "Synchronize pharmacy records",
            description = "Applies UUID-based, pharmacy-scoped synchronization for users, products, batches, sales, sale items, and app settings.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Synchronization completed and committed"),
                    @ApiResponse(responseCode = "400", description = "Invalid payload or missing relationship", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "409", description = "Record ownership or unique reference conflict detected", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class))),
                    @ApiResponse(responseCode = "500", description = "Unexpected server error", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiError.class)))
            }
    )
    public ResponseEntity<SyncResponse> synchronize(@Valid @RequestBody SyncRequest request) {
        log.debug("Received synchronization request for pharmacy {}", request.pharmacyId());
        UUID activityId = startActivity(request);

        SyncResponse response;
        try {
            response = syncService.synchronize(request);
        } catch (RuntimeException failure) {
            recordFailure(activityId, failure);
            throw failure;
        }

        recordSuccess(activityId, response);
        return ResponseEntity.ok(response);
    }

    private UUID startActivity(SyncRequest request) {
        try {
            return syncActivityService.start(request);
        } catch (RuntimeException auditFailure) {
            log.error("Could not start sync activity audit for pharmacy {}", request.pharmacyId(), auditFailure);
            return null;
        }
    }

    private void recordSuccess(UUID activityId, SyncResponse response) {
        if (activityId == null) {
            return;
        }
        try {
            syncActivityService.markSuccessful(activityId, response);
        } catch (RuntimeException auditFailure) {
            log.error("Synchronization committed, but activity {} could not be marked successful", activityId, auditFailure);
        }
    }

    private void recordFailure(UUID activityId, RuntimeException failure) {
        if (activityId == null) {
            return;
        }
        try {
            syncActivityService.markFailed(activityId, failure);
        } catch (RuntimeException auditFailure) {
            log.error("Synchronization failed, but activity {} could not be marked failed", activityId, auditFailure);
        }
    }
}
