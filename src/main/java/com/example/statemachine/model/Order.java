package com.example.statemachine.model;

import com.example.statemachine.statemachine.state.OrderState;

public class Order {

    private Long id;
    private String product;
    private Integer quantity;
    private OrderState state;

    public Order(Long id, String product, Integer quantity) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.state = OrderState.CREATED;
    }

    public Long getId() {
        return id;
    }

    public String getProduct() {
        return product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }
}