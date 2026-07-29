package com.example.phamarcy_server.exception;

import org.springframework.http.HttpStatus;

public class MissingSyncRelationshipException extends ApiException {

    public MissingSyncRelationshipException(String recordType, Object recordId, String relatedType, Object relatedId) {
        super(HttpStatus.BAD_REQUEST, recordType + " record " + recordId + " references missing " + relatedType + " record " + relatedId);
    }
}
