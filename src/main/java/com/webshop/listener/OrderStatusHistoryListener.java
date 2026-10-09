package com.webshop.listener;

import com.webshop.entity.Order;
import com.webshop.entity.OrderStatus;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.JdbcTemplate;

public class OrderStatusHistoryListener {

    private static final String INSERT_STATUS_CHANGE =
            "insert into OrderStatusHistory (order_id, previous_status, updated_status, created_at) values (?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public OrderStatusHistoryListener(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostLoad
    @PostPersist
    void rememberStoredStatus(Order order) {
        order.setStoredStatus(order.getStatus());
    }


    @PostUpdate
    void logStatusChange(Order order) {
        if (order.getStatus() == order.getStoredStatus()) {
            return;
        }
        jdbcTemplate.update(INSERT_STATUS_CHANGE,
                order.getId(), nameOf(order.getStoredStatus()), nameOf(order.getStatus()), LocalDateTime.now());
        order.setStoredStatus(order.getStatus());
    }

    private String nameOf(OrderStatus status) {
        return status == null ? null : status.name();
    }
}
