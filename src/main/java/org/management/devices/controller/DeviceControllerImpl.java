package org.management.devices.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.management.devices.dto.ErrorResponse;
import org.management.devices.service.DeviceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
@Tag(name = "Device Management", description = "APIs for managing devices")
public class DeviceControllerImpl implements DeviceController {

    private final DeviceService deviceService;

    @Override
    @PostMapping
    @Operation(summary = "Create a new device", description = "Creates a new device with the provided information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data (e.g., missing required fields)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeviceResponse> create(@Valid @RequestBody DeviceCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceService.create(request));
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Get device by ID", description = "Retrieves a specific device by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device found",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeviceResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(deviceService.getById(id));
    }

    @Override
    @GetMapping
    @Operation(summary = "Get all devices", description = "Retrieves all devices with optional filtering by brand and/or state")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devices retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @Operation(summary = "Update device", description = "Fully updates an existing device (all fields required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Cannot update brand of IN_USE device",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeviceResponse> update(@PathVariable UUID id, @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.update(id, request));
    }

    @Override
    @PatchMapping("/{id}")
    @Operation(summary = "Partially update device", description = "Partially updates an existing device (only provided fields are updated)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(schema = @Schema(implementation = DeviceResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Cannot update brand of IN_USE device",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<DeviceResponse> partialUpdate(@PathVariable UUID id, @Valid @RequestBody DeviceUpdateRequest request) {
        return ResponseEntity.ok(deviceService.partialUpdate(id, request));
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete device", description = "Deletes a device by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Device not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict - Cannot delete IN_USE device",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
