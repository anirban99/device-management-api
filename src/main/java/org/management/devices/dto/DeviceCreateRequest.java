package org.management.devices.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.management.devices.domain.DeviceState;

public record DeviceCreateRequest(
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Brand is required")
        String brand,
        DeviceState state
) {}

