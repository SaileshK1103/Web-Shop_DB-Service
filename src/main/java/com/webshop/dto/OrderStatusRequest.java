package com.webshop.dto;

import com.webshop.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

// Request DTO for changing the status of an order
public record OrderStatusRequest(@NotNull OrderStatus status) {
}
