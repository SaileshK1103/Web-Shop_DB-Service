package com.webshop.repository;

import com.webshop.entity.Product;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

// The list methods fetch category and supplier together with the products to avoid one extra query per product
public interface ProductRepository extends JpaRepository<Product, Integer> {

    boolean existsByCategoryId(Integer categoryId);

    // Use entity graph to load the category and supplier and avoid any lazy loading proxy errors
    @EntityGraph(attributePaths = {"category", "supplier"})
    @Override
    List<Product> findAll();

    // Use entity graph to load the category and supplier and avoid any lazy loading proxy errors
    @EntityGraph(attributePaths = {"category", "supplier"})
    List<Product> findByNameContainingIgnoreCase(String name);

}
