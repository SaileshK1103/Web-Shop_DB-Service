package com.webshop.entity;

// Life cycle of an order: PENDING -> CONFIRMED -> SHIPPED -> DELIVERED.
public enum OrderStatus {
    NEW("new"),
    PENDING("pending"),
    CONFIRMED("confirmed"),
    SHIPPED("shipped"),
    DELIVERED("delivered"),
    CANCELLED("cancelled");

    private final String value;

    OrderStatus(String value) {
        this.value = value;
    }

    public static OrderStatus fromString(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
            String.format("Unknown order status: %s",value)
        );
    }

    @Override
    public String toString() {
        return this.value;
    }

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
