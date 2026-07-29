package com.example.phamarcy_server.service;

import com.example.phamarcy_server.dto.SyncRequest;
import com.example.phamarcy_server.dto.SyncResponse;
import java.util.UUID;

public interface SyncActivityService {

    UUID start(SyncRequest request);
    void markSuccessful(UUID activityId, SyncResponse response);
    void markFailed(UUID activityId, RuntimeException failure);
}
