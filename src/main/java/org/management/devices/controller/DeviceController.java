package org.management.devices.controller;

import jakarta.validation.Valid;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface DeviceController {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request);
}
