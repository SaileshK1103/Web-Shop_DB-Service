package com.webshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.webshop.dto.OrderItemRequest;
import com.webshop.dto.OrderRequest;
import com.webshop.entity.Customer;
import com.webshop.entity.Product;
import com.webshop.exception.ConflictException;
import com.webshop.repository.CustomerRepository;
import com.webshop.repository.OrderRepository;
import com.webshop.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

// Not @Transactional on purpose: the rollback of OrderService.create must really happen in the database,
// so the test commits its own data and removes it afterwards
@SpringBootTest
@ActiveProfiles("test")
class OrderTransactionTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Customer customer;
    private Product inStock;
    private Product almostSoldOut;

    @AfterEach
    void removeCommittedData() {
        orderRepository.deleteAll();
        productRepository.deleteAll(List.of(inStock, almostSoldOut));
        customerRepository.delete(customer);
    }

    @Test
    void orderThatFailsOnOneItemChangesNoStockAndStoresNoOrder() {
        customer = customerRepository.save(new Customer("Ada", "Lovelace", "rollback@example.com", null));
        inStock = productRepository.save(new Product("Clean Code", null, new BigDecimal("39.90"), 10));
        almostSoldOut = productRepository.save(new Product("Refactoring", null, new BigDecimal("45.00"), 1));
        OrderRequest request = new OrderRequest(customer.getId(), null, List.of(
                new OrderItemRequest(inStock.getId(), 5),
                new OrderItemRequest(almostSoldOut.getId(), 2)));

        assertThatThrownBy(() -> orderService.create(request)).isInstanceOf(ConflictException.class);

        assertThat(productRepository.findById(inStock.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
        assertThat(productRepository.findById(almostSoldOut.getId()).orElseThrow().getStockQuantity()).isEqualTo(1);
        assertThat(orderRepository.existsByCustomerId(customer.getId())).isFalse();
    }
}
