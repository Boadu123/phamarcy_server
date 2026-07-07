package com.example.phamarcy_server.service;

import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import com.example.phamarcy_server.security.AuthenticatedPharmacy;

public interface SyncService {

    SyncResponse synchronize(SyncRequest request, AuthenticatedPharmacy authenticatedPharmacy);
}