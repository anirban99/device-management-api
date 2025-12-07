package org.management.devices.controller;

import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.UUID;

public interface DeviceController {

    ResponseEntity<DeviceResponse> create(DeviceCreateRequest request);

    ResponseEntity<DeviceResponse> getById(UUID id);

    ResponseEntity<List<DeviceResponse>> getAll(String brand, String state);

    ResponseEntity<DeviceResponse> update(UUID id, DeviceUpdateRequest request);

    ResponseEntity<DeviceResponse> partialUpdate(UUID id, DeviceUpdateRequest request);

    ResponseEntity<Void> delete(UUID id);
}
