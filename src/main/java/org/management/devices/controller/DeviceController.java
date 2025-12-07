package org.management.devices.controller;

import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.springframework.http.ResponseEntity;
import java.util.UUID;

public interface DeviceController {

    ResponseEntity<DeviceResponse> create(DeviceCreateRequest request);

    ResponseEntity<DeviceResponse> getById(UUID id);
}
