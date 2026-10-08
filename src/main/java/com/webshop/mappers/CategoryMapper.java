package com.webshop.mappers;

import com.webshop.dto.CategoryRequest;
import com.webshop.dto.CategoryResponse;
import com.webshop.entity.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryResponse toResponse(ProductCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }

    public ProductCategory toEntity(CategoryRequest request) {
        return new ProductCategory(request.name(), request.description());
    }

    public void updateEntity(ProductCategory category, CategoryRequest request) {
        category.setName(request.name());
        category.setDescription(request.description());
    }
}
