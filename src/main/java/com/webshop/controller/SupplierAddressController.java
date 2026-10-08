package com.webshop.controller;

import com.webshop.dto.AddressRequest;
import com.webshop.dto.AddressResponse;
import com.webshop.service.SupplierAddressService;
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
@RequestMapping("/api/suppliers/{supplierId}/addresses")
public class SupplierAddressController {

    private final SupplierAddressService supplierAddressService;

    public SupplierAddressController(SupplierAddressService supplierAddressService) {
        this.supplierAddressService = supplierAddressService;
    }

    @GetMapping
    public List<AddressResponse> getAllSupplierAddresses(@PathVariable Integer supplierId) {
        return supplierAddressService.findAll(supplierId);
    }

    @GetMapping("/{addressId}")
    public AddressResponse findById(@PathVariable Integer supplierId, @PathVariable Integer addressId) {
        return supplierAddressService.findById(supplierId, addressId);
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable Integer supplierId, @Valid @RequestBody AddressRequest request) {
        AddressResponse created = supplierAddressService.create(supplierId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{addressId}")
    public AddressResponse update(
            @PathVariable Integer supplierId,
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressRequest request) {
        return supplierAddressService.update(supplierId, addressId, request);
    }

    @DeleteMapping("/{addressId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer supplierId, @PathVariable Integer addressId) {
        supplierAddressService.delete(supplierId, addressId);
    }
}
