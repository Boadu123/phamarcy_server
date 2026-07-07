package com.example.phamarcy_server.exception;

import org.springframework.http.HttpStatus;

public class InvalidApiTokenException extends ApiException {

    public InvalidApiTokenException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid or missing pharmacy API token");
    }
}