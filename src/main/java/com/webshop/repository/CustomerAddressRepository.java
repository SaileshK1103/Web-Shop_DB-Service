package com.webshop.repository;

import com.webshop.entity.CustomerAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Integer> {

    List<CustomerAddress> findByCustomerId(Integer customerId);

    Optional<CustomerAddress> findByIdAndCustomerId(Integer id, Integer customerId);
}
