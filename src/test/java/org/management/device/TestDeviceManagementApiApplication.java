package org.management.device;

import org.springframework.boot.SpringApplication;

public class TestDeviceManagementApiApplication {

    public static void main(String[] args) {
        SpringApplication.from(DeviceManagementApiApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
