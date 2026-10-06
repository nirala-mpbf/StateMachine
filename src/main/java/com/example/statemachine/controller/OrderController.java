package com.example.statemachine.controller;

import com.example.statemachine.dto.CreateOrderRequest;
import com.example.statemachine.dto.OrderEventRequest;
import com.example.statemachine.model.Order;
import com.example.statemachine.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order createOrder(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getOrder(orderId);
    }

    @PostMapping("/{orderId}/events")
    public Order processEvent( @PathVariable Long orderId, @RequestBody OrderEventRequest request) {

        return orderService.processEvent(
                orderId,
                request.event()
        );
    }
}