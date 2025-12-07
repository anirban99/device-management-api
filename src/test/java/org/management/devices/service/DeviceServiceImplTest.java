package org.management.devices.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.exception.DeviceNotFoundException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.management.devices.domain.Device;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.mapper.DeviceMapper;
import org.management.devices.repository.DeviceRepository;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceImplTest {

    private static final UUID DEVICE_ID = UUID.randomUUID();
    private static final String DEVICE_NAME = "iPhone 15";
    private static final String DEVICE_BRAND = "Apple";
    private static final DeviceState DEVICE_STATE = DeviceState.AVAILABLE;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private DeviceMapper mapper;

    @InjectMocks
    private DeviceServiceImpl deviceService;

    private DeviceCreateRequest createRequest;
    private Device device;
    private Device savedDevice;
    private DeviceResponse expectedResponse;

    @BeforeEach
    void setUp() {
        createRequest = createDeviceRequestWithState();
        device = createDevice();
        savedDevice = createSavedDevice();
        expectedResponse = createExpectedResponse();
    }

    @Test
    void create_ShouldReturnSavedDevice_WhenValidRequestProvided() {
        // Given
        when(mapper.toEntity(createRequest)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        DeviceResponse result = deviceService.create(createRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.name()).isEqualTo(DEVICE_NAME);
        assertThat(result.brand()).isEqualTo(DEVICE_BRAND);
        assertThat(result.state()).isEqualTo(DEVICE_STATE);
    }

    @Test
    void create_ShouldCallMapperToEntity_WithCorrectRequest() {
        // Given
        when(mapper.toEntity(createRequest)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        deviceService.create(createRequest);

        // Then
        verify(mapper, times(1)).toEntity(createRequest);
    }

    @Test
    void create_ShouldCallRepositorySave_WithMappedEntity() {
        // Given
        when(mapper.toEntity(createRequest)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        deviceService.create(createRequest);

        // Then
        verify(deviceRepository, times(1)).save(device);
    }

    @Test
    void create_ShouldCallMapperToResponse_WithSavedEntity() {
        // Given
        when(mapper.toEntity(createRequest)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        deviceService.create(createRequest);

        // Then
        verify(mapper, times(1)).toResponse(savedDevice);
    }

    @Test
    void create_ShouldUseDefaultAvailableState_WhenStateNotSpecified() {
        // Given
        DeviceCreateRequest request = createDeviceRequestWithoutState();
        Device device = createDeviceWithoutState();
        Device savedDevice = createSavedDeviceWithDefaultState();

        when(mapper.toEntity(request)).thenReturn(device);
        when(deviceRepository.save(device)).thenReturn(savedDevice);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        DeviceResponse result = deviceService.create(request);

        // Then
        assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        verify(mapper).toEntity(request);
        verify(deviceRepository).save(device);
    }

    @Test
    void getById_ShouldReturnDeviceResponse_WhenDeviceExists() {
        // Given
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        DeviceResponse result = deviceService.getById(DEVICE_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(DEVICE_ID);
        assertThat(result.name()).isEqualTo(DEVICE_NAME);
        assertThat(result.brand()).isEqualTo(DEVICE_BRAND);
        assertThat(result.state()).isEqualTo(DEVICE_STATE);
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(mapper, times(1)).toResponse(savedDevice);
    }

    @Test
    void getById_ShouldThrowDeviceNotFoundException_WhenDeviceNotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(DeviceNotFoundException.class, () -> {
            deviceService.getById(nonExistentId);
        });

        verify(deviceRepository, times(1)).findById(nonExistentId);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getById_ShouldCallRepositoryFindById_WithCorrectId() {
        // Given
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        deviceService.getById(DEVICE_ID);

        // Then
        verify(deviceRepository).findById(DEVICE_ID);
    }

    @Test
    void getById_ShouldCallMapperToResponse_WithFoundDevice() {
        // Given
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        deviceService.getById(DEVICE_ID);

        // Then
        verify(mapper).toResponse(argThat(d ->
                d.getId().equals(DEVICE_ID) &&
                        d.getName().equals(DEVICE_NAME) &&
                        d.getBrand().equals(DEVICE_BRAND) &&
                        d.getState().equals(DEVICE_STATE)
        ));
    }

    // Helper methods
    private DeviceCreateRequest createDeviceRequestWithState() {
        return new DeviceCreateRequest(DEVICE_NAME, DEVICE_BRAND, DEVICE_STATE);
    }

    private DeviceCreateRequest createDeviceRequestWithoutState() {
        return new DeviceCreateRequest(DEVICE_NAME, DEVICE_BRAND, null);
    }

    private Device createDevice() {
        Device device = new Device();
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        device.setState(DEVICE_STATE);
        return device;
    }

    private Device createDeviceWithoutState() {
        Device device = new Device();
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        return device;
    }

    private Device createSavedDevice() {
        Device device = new Device();
        device.setId(DEVICE_ID);
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        device.setState(DEVICE_STATE);
        device.setCreatedAt(Instant.now());
        return device;
    }

    private Device createSavedDeviceWithDefaultState() {
        Device device = new Device();
        device.setId(DEVICE_ID);
        device.setName(DEVICE_NAME);
        device.setBrand(DEVICE_BRAND);
        device.setState(DeviceState.AVAILABLE);
        device.setCreatedAt(Instant.now());
        return device;
    }

    private DeviceResponse createExpectedResponse() {
        return new DeviceResponse(DEVICE_ID, DEVICE_NAME, DEVICE_BRAND, DEVICE_STATE, Instant.now());
    }
}
