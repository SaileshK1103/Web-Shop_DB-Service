package com.webshop.repository;

import com.webshop.entity.Product;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

// The list methods fetch category and supplier together with the products to avoid one extra query per product
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByCategoryId(Integer categoryId);

    boolean existsBySupplierId(Integer supplierId);

    // Locks the product row until the transaction ends, so two orders placed at the
    // same time cannot both take the last items in stock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Product> findWithLockById(Integer id);

    // Use entity graph to load the category and supplier and avoid any lazy loading proxy errors
    @EntityGraph(attributePaths = {"category", "supplier"})
    @Override
    List<Product> findAll();

    // Utilizes the index for faster lookup
    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findByNameStartingWithAndPriceBetween(String name, BigDecimal minPrice, BigDecimal maxPrice);

}
