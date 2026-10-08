package com.webshop.dto;

import java.util.List;

// Response DTO for supplier data together with the addresses of the supplier
public record SupplierResponse(
        Integer id,
        String name,
        String contactName,
        String phone,
        String email,
        List<AddressResponse> addresses) {
}
