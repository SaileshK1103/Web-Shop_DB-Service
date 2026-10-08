package com.webshop.mappers;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.entity.Supplier;
import com.webshop.entity.SupplierAddress;
import org.springframework.stereotype.Component;

// Customer and supplier addresses are separate tables with the same columns, so they share the address DTOs
@Component
public class AddressMapper {

    public AddressResponse toResponse(CustomerAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getStreetAddress(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry());
    }

    public AddressResponse toResponse(SupplierAddress address) {
        return new AddressResponse(
                address.getId(),
                address.getStreetAddress(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry());
    }

    public CustomerAddress toEntity(AddressRequest request, Customer customer) {
        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        updateEntity(address, request);
        return address;
    }

    public SupplierAddress toEntity(AddressRequest request, Supplier supplier) {
        SupplierAddress address = new SupplierAddress();
        address.setSupplier(supplier);
        updateEntity(address, request);
        return address;
    }

    public void updateEntity(CustomerAddress address, AddressRequest request) {
        address.setStreetAddress(request.streetAddress());
        address.setPostalCode(request.postalCode());
        address.setCity(request.city());
        address.setCountry(request.country());
    }

    public void updateEntity(SupplierAddress address, AddressRequest request) {
        address.setStreetAddress(request.streetAddress());
        address.setPostalCode(request.postalCode());
        address.setCity(request.city());
        address.setCountry(request.country());
    }
}
