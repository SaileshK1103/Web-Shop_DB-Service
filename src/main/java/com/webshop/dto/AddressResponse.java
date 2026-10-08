package com.webshop.dto;

// Response DTO for address data of a customer or a supplier
public record AddressResponse(Integer id, String streetAddress, String postalCode, String city, String country) {
}
