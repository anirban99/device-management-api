package org.management.devices.service;

import lombok.RequiredArgsConstructor;
import org.management.devices.domain.Device;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.management.devices.exception.DeviceDeletionException;
import org.management.devices.exception.DeviceNotFoundException;
import org.management.devices.exception.DeviceUpdateValidationException;
import org.management.devices.mapper.DeviceMapper;
import org.management.devices.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Qualifier("deviceMapperImpl")
    private final DeviceMapper mapper;

    @Override
    public DeviceResponse create(DeviceCreateRequest request) {
        Device device = mapper.toEntity(request);
        return mapper.toResponse(deviceRepository.save(device));
    }

    @Override
    public DeviceResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    public List<DeviceResponse> getAll() {
        return deviceRepository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeviceResponse> getByBrand(String brand) {
        return deviceRepository.findByBrand(brand).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeviceResponse> getByState(String state) {
        DeviceState deviceState = DeviceState.valueOf(state.trim().toUpperCase());
        return deviceRepository.findByState(deviceState).stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<DeviceResponse> getByBrandAndState(String brand, String state) {
        DeviceState deviceState = DeviceState.valueOf(state.trim().toUpperCase());
        return deviceRepository.findByBrandAndState(brand, deviceState).stream().map(mapper::toResponse).toList();
    }

    @Override
    public DeviceResponse update(UUID id, DeviceUpdateRequest deviceUpdateRequest) {
        if (deviceUpdateRequest.name() == null || deviceUpdateRequest.brand() == null || deviceUpdateRequest.state() == null) {
            throw new DeviceUpdateValidationException("PUT request requires 'name', 'brand', and 'state' fields to be present.");
        }

        Device existingDevice = find(id);

        boolean isDeviceInUse = existingDevice.getState() == DeviceState.IN_USE;

        if (isDeviceInUse) {
            if (!deviceUpdateRequest.name().equals(existingDevice.getName())) {
                throw new DeviceUpdateValidationException("Cannot update 'name' for device " + id + " because its state is IN_USE.");
            }
            if (!deviceUpdateRequest.brand().equals(existingDevice.getBrand())) {
                throw new DeviceUpdateValidationException("Cannot update 'brand' for device " + id + " because its state is IN_USE.");
            }
        }

        existingDevice.setName(deviceUpdateRequest.name());
        existingDevice.setBrand(deviceUpdateRequest.brand());
        existingDevice.setState(deviceUpdateRequest.state());

        return mapper.toResponse(deviceRepository.save(existingDevice));
    }

    @Override
    public DeviceResponse partialUpdate(UUID id, DeviceUpdateRequest deviceUpdateRequest) {
        Device existingDevice = find(id);

        boolean isDeviceInUse = existingDevice.getState() == DeviceState.IN_USE;

        if (isDeviceInUse) {
            if (deviceUpdateRequest.name() != null && !deviceUpdateRequest.name().equals(existingDevice.getName())) {
                throw new DeviceUpdateValidationException("Cannot update 'name' for device " + id + " because its state is IN_USE.");
            }
            if (deviceUpdateRequest.brand() != null && !deviceUpdateRequest.brand().equals(existingDevice.getBrand())) {
                throw new DeviceUpdateValidationException("Cannot update 'brand' for device " + id + " because its state is IN_USE.");
            }
        }

        if (deviceUpdateRequest.name() != null) {
            existingDevice.setName(deviceUpdateRequest.name());
        }

        if (deviceUpdateRequest.brand() != null) {
            existingDevice.setBrand(deviceUpdateRequest.brand());
        }

        if (deviceUpdateRequest.state() != null) {
            existingDevice.setState(deviceUpdateRequest.state());
        }

        return mapper.toResponse(deviceRepository.save(existingDevice));
    }

    @Override
    public void delete(UUID id) {
        Device device = find(id);

        if (device.getState() == DeviceState.IN_USE) {
            throw new DeviceDeletionException("Cannot delete device with ID " + id + " because its state is IN_USE.");
        }

        deviceRepository.delete(device);
    }

    private Device find(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
    }
}
