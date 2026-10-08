package com.webshop.repository;

import com.webshop.entity.SupplierAddress;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierAddressRepository extends JpaRepository<SupplierAddress, Integer> {

    List<SupplierAddress> findBySupplierId(Integer supplierId);

    Optional<SupplierAddress> findByIdAndSupplierId(Integer id, Integer supplierId);
}
