package com.webshop.repository;

import com.webshop.entity.Customer;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    // Use entity graph to load the addresses together with the customers and avoid one extra query per customer
    @EntityGraph(attributePaths = {"addresses"})
    @Override
    List<Customer> findAll();
}
