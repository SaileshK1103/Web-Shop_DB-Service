package com.webshop.repository;

import com.webshop.entity.OrderItem;
import com.webshop.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    boolean existsByProductId(Integer productId);
}
