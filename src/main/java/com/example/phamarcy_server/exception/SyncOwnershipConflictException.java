package com.example.phamarcy_server.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SyncOwnershipConflictException extends ApiException {

    public SyncOwnershipConflictException(String resourceType, UUID resourceId) {
        super(HttpStatus.CONFLICT, resourceType + " record belongs to a different pharmacy: " + resourceId);
    }
}