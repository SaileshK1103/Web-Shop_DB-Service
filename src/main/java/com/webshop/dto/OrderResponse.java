package com.webshop.dto;

import com.webshop.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// Response DTO for order data together with its customer, shipping address, items and calculated total
public record OrderResponse(
        Integer id,
        LocalDateTime orderDate,
        LocalDateTime deliveryDate,
        OrderStatus status,
        CustomerSummary customer,
        AddressResponse shippingAddress,
        List<OrderItemResponse> items,
        BigDecimal totalAmount) {

    public record CustomerSummary(Integer id, String firstName, String lastName, String email) {
    }

    public record OrderItemResponse(
            Integer productId, String productName, Integer quantity, BigDecimal unitPrice, BigDecimal lineTotal) {
    }
}
