package com.webshop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

// Request DTO for placing an order
public record OrderRequest(
        @NotNull Integer customerId,
        Integer shippingAddressId,
        @NotEmpty @Valid List<OrderItemRequest> items) {
}
