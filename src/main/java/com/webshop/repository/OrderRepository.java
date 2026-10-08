package com.webshop.repository;

import com.webshop.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    boolean existsByCustomerId(Integer customerId);

    boolean existsByShippingAddressId(Integer shippingAddressId);
}
