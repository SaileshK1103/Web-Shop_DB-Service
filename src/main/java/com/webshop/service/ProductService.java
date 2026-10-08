package com.webshop.service;

import com.webshop.dto.ProductRequest;
import com.webshop.dto.ProductResponse;
import com.webshop.entity.Product;
import com.webshop.entity.ProductCategory;
import com.webshop.entity.Supplier;
import com.webshop.exception.BadRequestException;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.ProductMapper;
import com.webshop.repository.OrderItemRepository;
import com.webshop.repository.ProductCategoryRepository;
import com.webshop.repository.ProductRepository;
import com.webshop.repository.SupplierRepository;
import com.webshop.Utils;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final SupplierRepository supplierRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductMapper productMapper;

    public ProductService(
            ProductRepository productRepository,
            ProductCategoryRepository productCategoryRepository,
            SupplierRepository supplierRepository,
            OrderItemRepository orderItemRepository,
            ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.supplierRepository = supplierRepository;
        this.orderItemRepository = orderItemRepository;
        this.productMapper = productMapper;
    }


    public List<ProductResponse> search(String name) {
        return findProducts(name).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    public ProductResponse findById(Integer id) {
        return productMapper.toResponse(getProduct(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(
                request, findCategory(request.categoryId()), findSupplier(request.supplierId()));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Integer id, ProductRequest request) {
        Product product = getProduct(id);
        productMapper.updateEntity(
                product, request, findCategory(request.categoryId()), findSupplier(request.supplierId()));
        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Integer id) {
        if (orderItemRepository.existsByProductId(id)) {
            // Existing orders still refer to the product, so it cannot be deleted
            throw new ConflictException(String.format("Product with id %d appears on orders and cannot be deleted", id));
        }
        productRepository.deleteById(id);
    }

    // Find product by product name
    private List<Product> findProducts(String name) {
        if (Utils.isNonEmptyString(name)) {
            return productRepository.findByNameContainingIgnoreCase(name.strip());
        }
        return productRepository.findAll();
    }

    private Product getProduct(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    // Category is optional on a product defined by nullable in the db schema, but when an id is given it must exist
    private ProductCategory findCategory(Integer categoryId) {
        if (categoryId == null) {
            return null;
        }
        return productCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new BadRequestException(String.format("Category with id %d does not exist", categoryId)));
    }

    // Supplier is optional on a product defined by nullable in the db schema, but when an id is given it must exist
    private Supplier findSupplier(Integer supplierId) {
        if (supplierId == null) {
            return null;
        }
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new BadRequestException(String.format("Supplier with id %d does not exist", supplierId)));
    }
}
