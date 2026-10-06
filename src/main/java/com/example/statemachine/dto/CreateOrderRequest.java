package com.example.statemachine.dto;

public record CreateOrderRequest(
        String product,
        Integer quantity) {

}
