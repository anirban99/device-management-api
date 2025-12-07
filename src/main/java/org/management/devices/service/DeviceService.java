package org.management.devices.service;

import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;

import java.util.List;
import java.util.UUID;

public interface DeviceService {
    DeviceResponse create(DeviceCreateRequest request);

    DeviceResponse getById(UUID id);

    List<DeviceResponse> getAll();

    List<DeviceResponse> getByBrand(String brand);

    List<DeviceResponse> getByState(String state);

    List<DeviceResponse> getByBrandAndState(String brand, String state);

    void delete(UUID id);
}
