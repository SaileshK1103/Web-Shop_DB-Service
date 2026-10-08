package com.webshop.dto;

import java.util.List;

// Response DTO for customer data together with the addresses of the customer
public record CustomerResponse(
        Integer id,
        String firstName,
        String lastName,
        String email,
        String phone,
        List<AddressResponse> addresses) {
}
