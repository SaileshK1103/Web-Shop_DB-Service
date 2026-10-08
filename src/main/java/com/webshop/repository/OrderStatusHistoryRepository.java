package com.webshop.repository;

import com.webshop.entity.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {

    // The id is an auto increment, so ordering by id returns the changes oldest first
    List<OrderStatusHistory> findByOrderIdOrderById(Integer orderId);
}
