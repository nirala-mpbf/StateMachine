package com.example.statemachine.exception;

import com.example.statemachine.statemachine.event.OrderEvent;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class InvalidOrderTransitionException extends ApiException {

    private final List<OrderEvent> allowedEvents;

    public InvalidOrderTransitionException(String message, List<OrderEvent> allowedEvents) {
        super(message);
        this.allowedEvents = allowedEvents;
    }

    public List<OrderEvent> getAllowedEvents() {
        return allowedEvents;
    }

    @Override
    public HttpStatus status() {
        return HttpStatus.CONFLICT;
    }

    @Override
    public String title() {
        return "Invalid Order Transition";
    }

    @Override
    public Map<String, Object> properties() {
        return Map.of("allowedEvents", allowedEvents);
    }
}