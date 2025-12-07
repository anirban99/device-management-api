package org.management.devices.service;

import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;

public interface DeviceService {
    DeviceResponse create(DeviceCreateRequest request);
}
