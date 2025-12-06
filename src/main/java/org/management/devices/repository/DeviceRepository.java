package org.management.devices.repository;

import org.management.devices.domain.Device;
import org.management.devices.domain.DeviceState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByBrand(String brand);

    List<Device> findByState(DeviceState state);
}

