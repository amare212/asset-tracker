package com.example.Demo.dto;

import com.example.Demo.entity.AssetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AssetRequest(
        @NotBlank(message = "name must not be blank")
        String name,

        @NotBlank(message = "serialNumber is required")
        String serialNumber,

        @NotNull(message = "categoryId is required")
        Long categoryId,

        // optional: AVAILABLE (default) or MAINTENANCE
        AssetStatus status) {
}
