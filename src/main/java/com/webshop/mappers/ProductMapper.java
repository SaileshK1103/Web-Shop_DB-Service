package com.webshop.mappers;

import com.webshop.dto.ProductRequest;
import com.webshop.dto.ProductResponse;
import com.webshop.dto.ProductResponse.CategorySummary;
import com.webshop.dto.ProductResponse.SupplierSummary;
import com.webshop.entity.Product;
import com.webshop.entity.ProductCategory;
import com.webshop.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                toCategorySummary(product.getCategory()),
                toSupplierSummary(product.getSupplier()));
    }

    public Product toEntity(ProductRequest request, ProductCategory category, Supplier supplier) {
        Product product = new Product();
        updateEntity(product, request, category, supplier);
        return product;
    }

    public void updateEntity(Product product, ProductRequest request, ProductCategory category, Supplier supplier) {
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setCategory(category);
        product.setSupplier(supplier);
    }

    private CategorySummary toCategorySummary(ProductCategory category) {
        return category == null ? null : new CategorySummary(category.getId(), category.getName());
    }

    private SupplierSummary toSupplierSummary(Supplier supplier) {
        return supplier == null ? null : new SupplierSummary(supplier.getId(), supplier.getName());
    }
}
