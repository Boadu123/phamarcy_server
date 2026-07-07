package com.example.phamarcy_server.exception;

import org.springframework.http.HttpStatus;

public class PharmacyMismatchException extends ApiException {

    public PharmacyMismatchException() {
        super(HttpStatus.FORBIDDEN, "Payload pharmacy_id does not match authenticated pharmacy");
    }
}