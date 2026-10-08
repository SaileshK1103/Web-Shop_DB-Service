package com.webshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request DTO for creating and updating an address of a customer or a supplier
public record AddressRequest(
        @NotBlank @Size(max = 255) String streetAddress,
        @Size(max = 20) String postalCode,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String country) {
}
