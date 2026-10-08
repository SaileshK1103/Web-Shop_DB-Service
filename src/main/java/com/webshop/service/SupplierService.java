package com.webshop.service;

import com.webshop.dto.SupplierRequest;
import com.webshop.dto.SupplierResponse;
import com.webshop.entity.Supplier;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.SupplierMapper;
import com.webshop.repository.ProductRepository;
import com.webshop.repository.SupplierRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final SupplierMapper supplierMapper;

    public SupplierService(
            SupplierRepository supplierRepository, ProductRepository productRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.productRepository = productRepository;
        this.supplierMapper = supplierMapper;
    }

    public List<SupplierResponse> findAll() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toResponse)
                .toList();
    }

    public SupplierResponse findById(Integer id) {
        return supplierMapper.toResponse(getSupplier(id));
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        Supplier supplier = supplierRepository.save(supplierMapper.toEntity(request));
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public SupplierResponse update(Integer id, SupplierRequest request) {
        Supplier supplier = getSupplier(id);
        supplierMapper.updateEntity(supplier, request);
        return supplierMapper.toResponse(supplier);
    }

    @Transactional
    public void delete(Integer id) {
        if (productRepository.existsBySupplierId(id)) {
            // Products still refer to the supplier, so it cannot be deleted
            throw new ConflictException(String.format("Supplier with id %d still has products and cannot be deleted", id));
        }
        supplierRepository.deleteById(id);
    }

    private Supplier getSupplier(Integer id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", id));
    }
}
