package org.management.devices.dto;

import org.management.devices.domain.DeviceState;
import java.time.Instant;

public record DeviceResponse(
        Long id,
        String name,
        String brand,
        DeviceState state,
        Instant createdAt
) {}

