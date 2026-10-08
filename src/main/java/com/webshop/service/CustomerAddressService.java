package com.webshop.service;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.entity.Customer;
import com.webshop.entity.CustomerAddress;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.AddressMapper;
import com.webshop.repository.CustomerAddressRepository;
import com.webshop.repository.CustomerRepository;
import com.webshop.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final AddressMapper addressMapper;

    public CustomerAddressService(
            CustomerAddressRepository customerAddressRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            AddressMapper addressMapper) {
        this.customerAddressRepository = customerAddressRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.addressMapper = addressMapper;
    }

    public List<AddressResponse> findAll(Integer customerId) {
        getCustomer(customerId);
        return customerAddressRepository.findByCustomerId(customerId).stream()
                .map(addressMapper::toResponse)
                .toList();
    }

    public AddressResponse findById(Integer customerId, Integer addressId) {
        return addressMapper.toResponse(getAddress(customerId, addressId));
    }

    @Transactional
    public AddressResponse create(Integer customerId, AddressRequest request) {
        CustomerAddress address = addressMapper.toEntity(request, getCustomer(customerId));
        return addressMapper.toResponse(customerAddressRepository.save(address));
    }

    @Transactional
    public AddressResponse update(Integer customerId, Integer addressId, AddressRequest request) {
        CustomerAddress address = getAddress(customerId, addressId);
        addressMapper.updateEntity(address, request);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public void delete(Integer customerId, Integer addressId) {
        CustomerAddress address = getAddress(customerId, addressId);
        if (orderRepository.existsByShippingAddressId(addressId)) {
            // Existing orders were shipped to the address, so it cannot be deleted
            throw new ConflictException(
                    String.format("Address with id %d is the shipping address of an order and cannot be deleted", addressId));
        }
        customerAddressRepository.delete(address);
    }

    private Customer getCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", customerId));
    }

    // An address is only found through the customer it belongs to
    private CustomerAddress getAddress(Integer customerId, Integer addressId) {
        return customerAddressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", addressId));
    }
}
