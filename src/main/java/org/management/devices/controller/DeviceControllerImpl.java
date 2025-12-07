package org.management.devices.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.management.devices.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceControllerImpl implements DeviceController { // <-- Implements the contract

    private final DeviceService deviceService;

    @Override
    @PostMapping
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(request));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @Override
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getAll(@RequestParam(required = false) String brand,
                                                       @RequestParam(required = false) String state) {
        if (brand != null && !brand.isEmpty() && state != null && !state.isEmpty()) {
            return ResponseEntity.ok(deviceService.getByBrandAndState(brand, state));
        }

        if (brand != null && !brand.isEmpty()) return ResponseEntity.ok(deviceService.getByBrand(brand));
        if (state != null && !state.isEmpty()) return ResponseEntity.ok(deviceService.getByState(state));

        return ResponseEntity.ok(deviceService.getAll());
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> update(@PathVariable UUID id, @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @Override
    @PatchMapping("/{id}")
    public ResponseEntity<DeviceResponse> partialUpdate(@PathVariable UUID id, @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.partialUpdate(id, request));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
