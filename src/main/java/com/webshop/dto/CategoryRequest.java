package com.webshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Response DTO for category data
public record CategoryRequest(
        @NotBlank @Size(max = 100) String name,
        String description) {
}
