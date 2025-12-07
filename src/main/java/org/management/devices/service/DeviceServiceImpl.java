package org.management.devices.service;

import lombok.RequiredArgsConstructor;
import org.management.devices.domain.Device;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.mapper.DeviceMapper;
import org.management.devices.repository.DeviceRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceMapper mapper;

    @Override
    public DeviceResponse create(DeviceCreateRequest request) {
        Device device = mapper.toEntity(request);
        return mapper.toResponse(deviceRepository.save(device));
    }
}
