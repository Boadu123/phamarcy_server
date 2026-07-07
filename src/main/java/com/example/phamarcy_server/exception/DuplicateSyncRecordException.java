package com.example.phamarcy_server.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DuplicateSyncRecordException extends ApiException {

    public DuplicateSyncRecordException(String resourceType, UUID resourceId) {
        super(HttpStatus.BAD_REQUEST, "Duplicate " + resourceType + " id in synchronization payload: " + resourceId);
    }
}