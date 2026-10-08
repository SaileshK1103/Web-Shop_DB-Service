package com.webshop.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// Request DTO for one product line of a new order
public record OrderItemRequest(
        @NotNull Integer productId,
        @NotNull @Positive Integer quantity) {
}
