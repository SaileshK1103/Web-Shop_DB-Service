package com.webshop.mappers;

import com.webshop.dto.SupplierRequest;
import com.webshop.dto.SupplierResponse;
import com.webshop.entity.Supplier;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    private final AddressMapper addressMapper;

    public SupplierMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(
                supplier.getId(),
                supplier.getName(),
                supplier.getContactName(),
                supplier.getPhone(),
                supplier.getEmail(),
                supplier.getAddresses().stream().map(addressMapper::toResponse).toList());
    }

    public Supplier toEntity(SupplierRequest request) {
        return new Supplier(request.name(), request.contactName(), request.email(), request.phone());
    }

    public void updateEntity(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.name());
        supplier.setContactName(request.contactName());
        supplier.setPhone(request.phone());
        supplier.setEmail(request.email());
    }
}
