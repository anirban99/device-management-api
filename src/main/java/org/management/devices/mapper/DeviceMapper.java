package org.management.devices.mapper;

import org.management.devices.domain.Device;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.dto.DeviceResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface DeviceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "state", source = "state", qualifiedByName = "mapState")
    Device toEntity(DeviceCreateRequest request);

    DeviceResponse toResponse(Device device);

    @Named("mapState")
    default DeviceState mapState(DeviceState state) {
        return state != null ? state : DeviceState.AVAILABLE;
    }
}
