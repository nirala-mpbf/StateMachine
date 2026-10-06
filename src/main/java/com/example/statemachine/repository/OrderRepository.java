package com.example.statemachine.repository;


import com.example.statemachine.model.Order;
import com.example.statemachine.statemachine.state.OrderState;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long save(Order order) {

        String sql = """
            INSERT INTO orders
                (product, quantity, state)
            VALUES
                (?, ?, ?)
            RETURNING id
            """;

        return jdbcTemplate.queryForObject(
                sql,
                Long.class,
                order.getProduct(),
                order.getQuantity(),
                order.getState().name()
        );
    }

    public Optional<Order> findById(Long orderId)     {

        String sql = """
                SELECT id, product, quantity, state
                FROM orders
                WHERE id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
                    Order order = new Order(
                            rs.getLong("id"),
                            rs.getString("product"),
                            rs.getInt("quantity")
                    );
                    order.setState(OrderState.valueOf(rs.getString("state")));
                    return order;
                },orderId)
                .stream().findFirst();
    }

    public void updateState(
            Long orderId,
            OrderState state) {

        String sql = """
                UPDATE orders
                SET state = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE id = ?
                """;

        jdbcTemplate.update(
                sql,
                state.name(),
                orderId
        );
    }
}
