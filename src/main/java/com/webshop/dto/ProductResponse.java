package com.webshop.dto;

import java.math.BigDecimal;

// Response DTO for product data, category and supplier are null when the product has none
public record ProductResponse(
        Integer id,
        String name,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        CategorySummary category,
        SupplierSummary supplier) {

    public record CategorySummary(Integer id, String name) {
    }

    public record SupplierSummary(Integer id, String name) {
    }
}
