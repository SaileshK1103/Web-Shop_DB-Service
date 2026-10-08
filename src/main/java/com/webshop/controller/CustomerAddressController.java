package com.webshop.controller;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.service.CustomerAddressService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers/{customerId}/addresses")
public class CustomerAddressController {

    private final CustomerAddressService customerAddressService;

    public CustomerAddressController(CustomerAddressService customerAddressService) {
        this.customerAddressService = customerAddressService;
    }

    @GetMapping
    public List<AddressResponse> getAddresses(@PathVariable Integer customerId) {
        return customerAddressService.findAll(customerId);
    }

    @GetMapping("/{addressId}")
    public AddressResponse findById(@PathVariable Integer customerId, @PathVariable Integer addressId) {
        return customerAddressService.findById(customerId, addressId);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable Integer customerId, @Valid @RequestBody AddressRequest request) {
        AddressResponse created = customerAddressService.create(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(
            @PathVariable Integer customerId,
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressRequest request) {
        return customerAddressService.update(customerId, addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer customerId, @PathVariable Integer addressId) {
        customerAddressService.delete(customerId, addressId);
    }
}
