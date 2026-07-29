package com.example.phamarcy_server.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class DuplicateStockReferenceException extends ApiException {

    public DuplicateStockReferenceException(UUID pharmacyId, String stockReference) {
        super(HttpStatus.CONFLICT, "Batch stock_reference must be unique for pharmacy " + pharmacyId + ": " + stockReference);
    }
}
