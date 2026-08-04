package com.example.phamarcy_server.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class SaleNotFoundException extends ApiException {

    public SaleNotFoundException(UUID pharmacyId, UUID saleId) {
        super(HttpStatus.NOT_FOUND, "Sale not found for pharmacy " + pharmacyId + ": " + saleId);
    }
}
