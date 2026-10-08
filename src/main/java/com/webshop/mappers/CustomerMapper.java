package com.webshop.mappers;

import com.webshop.dto.CustomerRequest;
import com.webshop.dto.CustomerResponse;
import com.webshop.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    private final AddressMapper addressMapper;

    public CustomerMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddresses().stream().map(addressMapper::toResponse).toList());
    }

    public Customer toEntity(CustomerRequest request) {
        return new Customer(request.firstName(), request.lastName(), request.email(), request.phone());
    }

    public void updateEntity(Customer customer, CustomerRequest request) {
        customer.setFirstName(request.firstName());
        customer.setLastName(request.lastName());
        customer.setEmail(request.email());
        customer.setPhone(request.phone());
    }
}
