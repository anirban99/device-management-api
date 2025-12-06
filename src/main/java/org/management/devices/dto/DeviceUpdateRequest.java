package org.management.devices.dto;

import org.management.devices.domain.DeviceState;

public record DeviceUpdateRequest(
        String name,
        String brand,
        DeviceState state
) {}

