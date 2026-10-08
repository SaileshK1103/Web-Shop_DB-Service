package com.webshop.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Request DTO for creating and updating a supplier
public record SupplierRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 100) String contactName,
        @Size(max = 30) String phone,
        @Email @Size(max = 255) String email) {
}
