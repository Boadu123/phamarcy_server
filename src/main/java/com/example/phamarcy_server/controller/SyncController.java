package com.example.phamarcy_server.controller;

import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;
import com.example.phamarcy_server.service.SyncService;
import com.example.phamarcy_server.util.ApiPaths;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SyncController {

    private static final Logger log = LoggerFactory.getLogger(SyncController.class);

    private final SyncService syncService;

    public SyncController(SyncService syncService) {
        this.syncService = syncService;
    }

    @PostMapping(ApiPaths.SYNC)
    public ResponseEntity<SyncResponse> synchronize(
            @RequestAttribute(AuthenticatedPharmacy.REQUEST_ATTRIBUTE) AuthenticatedPharmacy authenticatedPharmacy,
            @Valid @RequestBody SyncRequest request
    ) {
        log.debug("Received synchronization request for pharmacy {}", request.pharmacyId());
        return ResponseEntity.ok(syncService.synchronize(request, authenticatedPharmacy));
    }
}