package com.example.phamarcy_server.service;

import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;

public interface SyncService {

    SyncResponse synchronize(SyncRequest request);
}
