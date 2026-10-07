package com.example.statemachine.service.impl;

import com.example.statemachine.dto.CreateOrderRequest;
import com.example.statemachine.exception.InvalidOrderTransitionException;
import com.example.statemachine.exception.NotFoundException;
import com.example.statemachine.model.Order;
import com.example.statemachine.repository.OrderRepository;
import com.example.statemachine.service.OrderService;
import com.example.statemachine.statemachine.event.OrderEvent;
import com.example.statemachine.statemachine.state.OrderState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class OrderServiceImpl implements OrderService {

    private final StateMachineFactory<OrderState, OrderEvent> stateMachineFactory;
    private final OrderRepository orderRepository;

    public OrderServiceImpl(StateMachineFactory<OrderState, OrderEvent> stateMachineFactory, OrderRepository orderRepository) {
        this.stateMachineFactory = stateMachineFactory;
        this.orderRepository = orderRepository;
    }

    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(null, request.product(), request.quantity());
        Long orderId = orderRepository.save(order);

        return getOrder(orderId);
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order #" + orderId + " not found"));
    }

    public Order processEvent(Long orderId, OrderEvent event) {

        Order order = getOrder(orderId);
        StateMachine<OrderState, OrderEvent> stateMachine = stateMachineFactory.getStateMachine("ORDER_" + orderId);
        stateMachine.stop();

        stateMachine.getStateMachineAccessor().doWithAllRegions(
                accessor ->
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
            List<OrderEvent> allowedEvents = getAllowedEventsOnCurrentState(stateMachine);
            throw new InvalidOrderTransitionException("Event " + event + " is not allowed for order #" + orderId + " in state " + order.getState(), allowedEvents);
        }

        OrderState newState = stateMachine.getState().getId();
        orderRepository.updateState(orderId, newState);
        return getOrder(orderId);
    }


    private List<OrderEvent> getAllowedEventsOnCurrentState(StateMachine<OrderState, OrderEvent> stateMachine) {
        OrderState currentState = stateMachine.getState().getId();
        return stateMachine.getTransitions()
                .stream()
                .filter(transition -> transition.getSource().getId() == currentState)
                .map(transition -> transition.getTrigger().getEvent())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }
}
