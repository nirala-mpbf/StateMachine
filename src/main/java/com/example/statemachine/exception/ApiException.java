package com.example.statemachine.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public abstract class ApiException extends RuntimeException {

    protected ApiException(String message) {
        super(message);
    }

    public abstract HttpStatus status();
    public abstract String title();
    public Map<String, Object> properties() {
        return Map.of();
    }
}