package com.example.phamarcy_server.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;

public class PharmacyNotFoundException extends ApiException {

    public PharmacyNotFoundException(UUID pharmacyId) {
        super(HttpStatus.NOT_FOUND, "Pharmacy not found: " + pharmacyId);
    }
}