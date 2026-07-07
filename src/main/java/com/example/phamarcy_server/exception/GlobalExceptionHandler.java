package com.example.phamarcy_server.exception;

import com.example.phamarcy_server.dto.ApiError;
import com.example.phamarcy_server.dto.ApiViolation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
        HttpStatus status = ex.getStatus();
        log.warn("Request rejected with {} at {}: {}", status.value(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiViolation> violations = new ArrayList<>();
        violations.addAll(ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ApiViolation(error.getField(), error.getDefaultMessage()))
                .toList());
        violations.addAll(ex.getBindingResult().getGlobalErrors().stream()
                .map(error -> new ApiViolation(error.getObjectName(), error.getDefaultMessage()))
                .toList());

        return validationResponse(request, violations);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiViolation> violations = ex.getConstraintViolations().stream()
                .map(violation -> new ApiViolation(violation.getPropertyPath().toString(), violation.getMessage()))
                .toList();
        return validationResponse(request, violations);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), ex.getMessage(), request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableMessage(HttpMessageNotReadableException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Malformed JSON request body", request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(Exception ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        log.error("Unexpected request failure at {}", request.getRequestURI(), ex);
        return ResponseEntity.status(status)
                .body(ApiError.of(status.value(), status.getReasonPhrase(), "Unexpected server error", request.getRequestURI()));
    }

    private ResponseEntity<ApiError> validationResponse(HttpServletRequest request, List<ApiViolation> violations) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiError.withViolations(
                        status.value(),
                        status.getReasonPhrase(),
                        "Validation failed",
                        request.getRequestURI(),
                        violations
                ));
    }
}