package com.example.statemachine.dto;


import com.example.statemachine.statemachine.event.OrderEvent;

public record OrderEventRequest(OrderEvent event ) {
}