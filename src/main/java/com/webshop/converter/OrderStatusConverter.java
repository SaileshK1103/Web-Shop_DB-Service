package com.webshop.converter;

import com.webshop.entity.OrderStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

// Stores an order status in the db and converts db values to enum
@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, String> {

    @Override
    public String convertToDatabaseColumn(OrderStatus status) {
        return status == null ? null : status.toString();
    }

    @Override
    public OrderStatus convertToEntityAttribute(String storedStatus) {
        if (storedStatus == null) {
            return null;
        }
        try {
            return OrderStatus.fromString(storedStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(String.format("Unknown order status in the database: %s", storedStatus), e);
        }
    }
}
