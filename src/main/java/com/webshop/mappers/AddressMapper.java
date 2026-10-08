package com.webshop.mappers;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.entity.Address;
import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.entity.Supplier;
import com.webshop.entity.SupplierAddress;
import org.springframework.stereotype.Component;

// Customer and supplier addresses share the Address base class, so one mapper and one pair of DTOs serve both
@Component
public class AddressMapper {

    public AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getId(),
                address.getStreetAddress(),
                address.getPostalCode(),
                address.getCity(),
                address.getCountry());
    }

    public CustomerAddress toEntity(AddressRequest request, Customer customer) {
        CustomerAddress address = new CustomerAddress(request.streetAddress(), request.postalCode(), request.city(), request.country());
        address.setCustomer(customer);
        return address;
    }

    public SupplierAddress toEntity(AddressRequest request, Supplier supplier) {
        SupplierAddress address = new SupplierAddress(request.streetAddress(), request.postalCode(), request.city(), request.country());
        address.setSupplier(supplier);
        return address;
    }

    public void updateEntity(Address address, AddressRequest request) {
        address.setStreetAddress(request.streetAddress());
        address.setPostalCode(request.postalCode());
        address.setCity(request.city());
        address.setCountry(request.country());
    }
}
