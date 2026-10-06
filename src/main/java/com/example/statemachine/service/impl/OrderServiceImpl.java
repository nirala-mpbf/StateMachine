package com.example.statemachine.service.impl;

import com.example.statemachine.dto.CreateOrderRequest;
import com.example.statemachine.model.Order;
import com.example.statemachine.repository.OrderRepository;
import com.example.statemachine.service.OrderService;
import com.example.statemachine.statemachine.event.OrderEvent;
import com.example.statemachine.statemachine.state.OrderState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderServiceImpl implements OrderService {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;

    private final OrderRepository orderRepository;

    public OrderServiceImpl(
            StateMachineFactory<OrderState, OrderEvent> stateMachineFactory,
            OrderRepository orderRepository) {

        this.stateMachineFactory = stateMachineFactory;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(null, request.product(), request.quantity() );
        Long orderId = orderRepository.save(order);

        return orderRepository.findById(orderId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Order processEvent(Long orderId, OrderEvent event) {

        Order order = orderRepository.findById(orderId);
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine("ORDER_" + orderId);
        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(accessor ->
                        accessor.resetStateMachine(
                                new DefaultStateMachineContext<>(
                                        order.getState(),
                                        null,
                                        null,
                                        null
                                )
                        )
                );
        stateMachine.start();

        boolean accepted = stateMachine.sendEvent(event);

        if (!accepted) {
            throw new IllegalStateException("Event " + event + " is not allowed for order " + orderId + " in state " + order.getState());
        }

        OrderState newState = stateMachine.getState().getId();
        orderRepository.updateState(orderId, newState);

        return orderRepository.findById(orderId);
    }
}
