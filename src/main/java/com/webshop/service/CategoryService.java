package com.webshop.service;

import com.webshop.dto.CategoryRequest;
import com.webshop.dto.CategoryResponse;
import com.webshop.entity.ProductCategory;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.CategoryMapper;
import com.webshop.repository.ProductCategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final CategoryMapper productCategoryMapper;

    public CategoryService(ProductCategoryRepository productCategoryRepository, CategoryMapper productCategoryMapper) {
        this.productCategoryRepository = productCategoryRepository;
        this.productCategoryMapper = productCategoryMapper;
    }

    public List<CategoryResponse> findAll() {
        return productCategoryRepository.findAll().stream()
                .map(productCategoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Integer id) {
        return productCategoryMapper.toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        ProductCategory category = productCategoryRepository.save(productCategoryMapper.toEntity(request));
        return productCategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(Integer id, CategoryRequest request) {
        ProductCategory category = getCategory(id);
        productCategoryMapper.updateEntity(category, request);
        return productCategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(Integer id) {
        ProductCategory category = getCategory(id);
        if (productCategoryRepository.existsByCategoryId(id)) {
            // If the category has associated products, we cannot delete it
            throw new ConflictException(String.format("Category with id %d still has products and cannot be deleted", id));
        }
        productCategoryRepository.delete(category);
    }

    private ProductCategory getCategory(Integer id) {
        return productCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}
