package org.management.devices.dto;

import org.management.devices.domain.DeviceState;

public record DeviceUpdateRequest(
        String name,
        String brand,
        @ValidDeviceState(allowNull = true)
        DeviceState state
) {}

