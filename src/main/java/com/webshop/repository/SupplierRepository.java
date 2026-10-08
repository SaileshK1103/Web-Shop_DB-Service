package com.webshop.repository;

import com.webshop.entity.Supplier;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Integer> {

    // Use entity graph to load the addresses together with the suppliers and avoid one extra query per supplier
    @EntityGraph(attributePaths = {"addresses"})
    @Override
    List<Supplier> findAll();
}
