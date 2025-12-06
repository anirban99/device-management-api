package org.management.devices.mapper;

import org.management.devices.domain.Device;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Device toEntity(DeviceCreateRequest request);

    DeviceResponse toResponse(Device device);
}

