package org.management.devices.controller;

import lombok.RequiredArgsConstructor;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceControllerImpl implements DeviceController { // <-- Implements the contract

    private final DeviceService deviceService;

    public ResponseEntity<DeviceResponse> create(DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(request));
    }
}
