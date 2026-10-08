package com.webshop.service;

import com.webshop.dto.CustomerRequest;
import com.webshop.dto.CustomerResponse;
import com.webshop.entity.Customer;
import com.webshop.exception.ConflictException;
import com.webshop.exception.ResourceNotFoundException;
import com.webshop.mappers.CustomerMapper;
import com.webshop.repository.CustomerRepository;
import com.webshop.repository.OrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final CustomerMapper customerMapper;

    public CustomerService(
            CustomerRepository customerRepository, OrderRepository orderRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.customerMapper = customerMapper;
    }

    public List<CustomerResponse> findAll() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    public CustomerResponse findById(Integer id) {
        return customerMapper.toResponse(getCustomer(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        if (customerRepository.existsByEmailIgnoreCase(request.email())) {
            throw emailInUse(request.email());
        }
        Customer customer = customerRepository.save(customerMapper.toEntity(request));
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public CustomerResponse update(Integer id, CustomerRequest request) {
        Customer customer = getCustomer(id);
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(request.email(), id)) {
            throw emailInUse(request.email());
        }
        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    public void delete(Integer id) {
        if (orderRepository.existsByCustomerId(id)) {
            // Existing orders still refer to the customer, so it cannot be deleted
            throw new ConflictException(String.format("Customer with id %d has orders and cannot be deleted", id));
        }
        customerRepository.deleteById(id);
    }

    private Customer getCustomer(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", id));
    }

    // The email column is unique in the db schema
    private ConflictException emailInUse(String email) {
        return new ConflictException(String.format("Email %s is already used by another customer", email));
    }
}
