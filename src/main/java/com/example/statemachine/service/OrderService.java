package com.example.statemachine.service;

import com.example.statemachine.dto.CreateOrderRequest;
import com.example.statemachine.model.Order;
import com.example.statemachine.statemachine.event.OrderEvent;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);
    Order getOrder(Long orderId);
    Order processEvent(Long orderId, OrderEvent event);

}
