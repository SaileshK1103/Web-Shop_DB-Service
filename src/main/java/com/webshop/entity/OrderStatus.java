package com.webshop.entity;

// Life cycle of an order: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED.
public enum OrderStatus {
    NEW,
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canChangeTo(OrderStatus next) {
        return switch (this) {
            case NEW -> next == PENDING || next == CANCELLED;
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == SHIPPED || next == CANCELLED;
            case SHIPPED -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }

    public OrderStatus next() {
        return switch (this) {
            case NEW -> PENDING;
            case PENDING -> CONFIRMED;
            case CONFIRMED -> SHIPPED;
            case SHIPPED -> DELIVERED;
            case DELIVERED, CANCELLED -> this;
        };
    }
}
