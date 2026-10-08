package com.webshop.repository;

import com.webshop.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// The list methods fetch customer, shipping address, items and their products together with the orders
// to avoid extra queries per order
public interface OrderRepository extends JpaRepository<Order, Integer> {

    boolean existsByCustomerId(Integer customerId);

    boolean existsByShippingAddressId(Integer shippingAddressId);

    @EntityGraph(attributePaths = {"customer", "shippingAddress", "orderItems", "orderItems.product"})
    @Override
    List<Order> findAll();

    @EntityGraph(attributePaths = {"customer", "shippingAddress", "orderItems", "orderItems.product"})
    List<Order> findByCustomerId(Integer customerId);
}
