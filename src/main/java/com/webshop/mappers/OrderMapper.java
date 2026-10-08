package com.webshop.mappers;

import com.webshop.dto.OrderResponse;
import com.webshop.dto.OrderResponse.CustomerSummary;
import com.webshop.dto.OrderResponse.OrderItemResponse;
import com.webshop.entity.Customer;
import com.webshop.entity.Order;
import com.webshop.entity.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    private final AddressMapper addressMapper;

    public OrderMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getDeliveryDate(),
                order.getStatus(),
                toCustomerSummary(order.getCustomer()),
                order.getShippingAddress() == null ? null : addressMapper.toResponse(order.getShippingAddress()),
                order.getOrderItems().stream().map(this::toItemResponse).toList(),
                order.getTotalAmount());
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getLineTotal());
    }

    private CustomerSummary toCustomerSummary(Customer customer) {
        return new CustomerSummary(
                customer.getId(), customer.getFirstName(), customer.getLastName(), customer.getEmail());
    }
}
