package com.webshop.service;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.entity.Supplier;
import com.webshop.entity.SupplierAddress;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.AddressMapper;
import com.webshop.repository.SupplierAddressRepository;
import com.webshop.repository.SupplierRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SupplierAddressService {

    private final SupplierAddressRepository supplierAddressRepository;
    private final SupplierRepository supplierRepository;
    private final AddressMapper addressMapper;

    public SupplierAddressService(
            SupplierAddressRepository supplierAddressRepository,
            SupplierRepository supplierRepository,
            AddressMapper addressMapper) {
        this.supplierAddressRepository = supplierAddressRepository;
        this.supplierRepository = supplierRepository;
        this.addressMapper = addressMapper;
    }

    public List<AddressResponse> findAll(Integer supplierId) {
        getSupplier(supplierId);
        return supplierAddressRepository.findBySupplierId(supplierId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    public AddressResponse findById(Integer supplierId, Integer addressId) {
        return addressMapper.toResponse(getAddress(supplierId, addressId));
    }

    @Transactional
    public AddressResponse create(Integer supplierId, AddressRequest request) {
        SupplierAddress address = addressMapper.toEntity(request, getSupplier(supplierId));
        return addressMapper.toResponse(supplierAddressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Integer supplierId, Integer addressId, AddressRequest request) {
        SupplierAddress address = getAddress(supplierId, addressId);
        addressMapper.updateEntity(address, request);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public void delete(Integer supplierId, Integer addressId) {
        supplierAddressRepository.delete(getAddress(supplierId, addressId));
    }

    private Supplier getSupplier(Integer supplierId) {
        return supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", supplierId));
    }

    // An address is only found through the supplier it belongs to
    private SupplierAddress getAddress(Integer supplierId, Integer addressId) {
        return supplierAddressRepository.findByIdAndSupplierId(addressId, supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }
}
