package com.example.statemachine.service.impl;

import com.example.statemachine.dto.CreateOrderRequest;
import com.example.statemachine.model.Order;
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

    private final Map<Long, Order> orders = new ConcurrentHashMap<>();

    private final AtomicLong orderIdGenerator = new AtomicLong(100);

    public OrderServiceImpl(StateMachineFactory<OrderState, OrderEvent> stateMachineFactory) {
        this.stateMachineFactory = stateMachineFactory;
    }

    @Override
    public Order createOrder(CreateOrderRequest request) {

        Long orderId = orderIdGenerator.incrementAndGet();
        Order order = new Order(
                orderId,
                request.product(),
                request.quantity()
        );

        orders.put(orderId, order);
        return order;
    }

    @Override
    public Order getOrder(Long orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found: " + orderId);
        }
        return order;
    }

    @Override
    public Order processEvent(Long orderId, OrderEvent event) {

        Order order = getOrder(orderId);
        StateMachine<OrderState, OrderEvent> stateMachine =  stateMachineFactory.getStateMachine("ORDER_" + orderId);

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
        order.setState(newState);

        System.out.println("StateMachine current state: " + stateMachine.getState().getId());

        return order;
    }
}
