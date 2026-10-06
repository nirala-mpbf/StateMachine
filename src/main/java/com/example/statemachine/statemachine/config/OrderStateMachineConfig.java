package com.example.statemachine.statemachine.config;

import com.example.statemachine.statemachine.event.OrderEvent;
import com.example.statemachine.statemachine.state.OrderState;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
//@EnableStateMachine
@EnableStateMachineFactory
public class OrderStateMachineConfig
        extends EnumStateMachineConfigurerAdapter<OrderState, OrderEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<OrderState, OrderEvent> states)
            throws Exception {

        states
                .withStates()
                .initial(OrderState.CREATED)
                .states(EnumSet.allOf(OrderState.class));
    }

    @Override
    public void configure(
            StateMachineTransitionConfigurer<OrderState, OrderEvent> transitions)
            throws Exception {

            transitions
                .withExternal()
                .source(OrderState.CREATED)
                .target(OrderState.PAID)  // CREATED -> PAID
                .event(OrderEvent.PAYMENT_SUCCESS)

                .and()
                .withExternal()
                .source(OrderState.PAID)
                .target(OrderState.SHIPPED)  // PAID -> SHIPPED
                .event(OrderEvent.SHIP)

                .and()
                .withExternal()
                .source(OrderState.SHIPPED)
                .target(OrderState.DELIVERED) // SHIPPED -> DELIVERED
                .event(OrderEvent.DELIVER)

                .and()
                .withExternal()
                .source(OrderState.CREATED)
                .target(OrderState.CANCELLED) // CREATED -> CANCELLED
                .event(OrderEvent.CANCEL)

                .and()
                .withExternal()
                .source(OrderState.PAID)
                .target(OrderState.CANCELLED) // PAID -> CANCELLED
                .event(OrderEvent.CANCEL);
    }
}