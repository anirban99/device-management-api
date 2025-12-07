package org.management.devices.service;

import lombok.RequiredArgsConstructor;
import org.management.devices.domain.Device;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.exception.DeviceNotFoundException;
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

    private Device find(UUID id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id: " + id));
    }
}
