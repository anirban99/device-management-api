package org.management.devices.service.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.management.devices.exception.DeviceDeletionException;
import org.management.devices.exception.DeviceNotFoundException;
import org.management.devices.exception.DeviceUpdateValidationException;
import org.management.devices.service.DeviceServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.management.devices.domain.Device;
import org.management.devices.dto.DeviceCreateRequest;
import org.management.devices.mapper.DeviceMapper;
import org.management.devices.repository.DeviceRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceUnitTest {

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

    @Test
    void getAll_ShouldReturnEmptyList_WhenNoDevicesExist() {
        // Given
        when(deviceRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<DeviceResponse> result = deviceService.getAll();

        // Then
        assertThat(result).isEmpty();
        verify(deviceRepository, times(1)).findAll();
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getAll_ShouldReturnSingleDevice_WhenOneDeviceExists() {
        // Given
        List<Device> devices = List.of(savedDevice);
        when(deviceRepository.findAll()).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        List<DeviceResponse> result = deviceService.getAll();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(DEVICE_ID);
        assertThat(result.get(0).name()).isEqualTo(DEVICE_NAME);
        assertThat(result.get(0).brand()).isEqualTo(DEVICE_BRAND);
        assertThat(result.get(0).state()).isEqualTo(DEVICE_STATE);
        verify(deviceRepository, times(1)).findAll();
        verify(mapper, times(1)).toResponse(savedDevice);
    }

    @Test
    void getAll_ShouldReturnMultipleDevices_WhenMultipleDevicesExist() {
        // Given
        UUID deviceIdTwo = UUID.randomUUID();
        Device deviceTwo = createSecondDevice(deviceIdTwo);
        DeviceResponse responseTwo = createSecondDeviceResponse(deviceIdTwo);

        List<Device> devices = List.of(savedDevice, deviceTwo);
        when(deviceRepository.findAll()).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);
        when(mapper.toResponse(deviceTwo)).thenReturn(responseTwo);

        // When
        List<DeviceResponse> result = deviceService.getAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(DEVICE_ID);
        assertThat(result.get(1).id()).isEqualTo(deviceIdTwo);
        verify(deviceRepository, times(1)).findAll();
        verify(mapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    void getAll_ShouldCallMapperForEachDevice() {
        // Given
        UUID deviceIdTwo = UUID.randomUUID();
        UUID deviceIdThree = UUID.randomUUID();
        Device deviceTwo = createSecondDevice(deviceIdTwo);
        Device deviceThree = createThirdDevice(deviceIdThree);

        List<Device> devices = List.of(savedDevice, deviceTwo, deviceThree);
        when(deviceRepository.findAll()).thenReturn(devices);
        when(mapper.toResponse(any(Device.class))).thenReturn(expectedResponse);

        // When
        deviceService.getAll();

        // Then
        verify(mapper, times(3)).toResponse(any(Device.class));
        verify(mapper).toResponse(savedDevice);
        verify(mapper).toResponse(deviceTwo);
        verify(mapper).toResponse(deviceThree);
    }

    @Test
    void getByBrand_ShouldReturnEmptyList_WhenNoBrandDevicesExist() {
        // Given
        when(deviceRepository.findByBrand(DEVICE_BRAND)).thenReturn(Collections.emptyList());

        // When
        List<DeviceResponse> result = deviceService.getByBrand(DEVICE_BRAND);

        // Then
        assertThat(result).isEmpty();
        verify(deviceRepository, times(1)).findByBrand(DEVICE_BRAND);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getByBrand_ShouldReturnMultipleDevices_WhenMultipleBrandDevicesExist() {
        // Given
        UUID deviceIdTwo = UUID.randomUUID();
        Device appleDeviceTwo = createAppleDevice(deviceIdTwo);
        DeviceResponse responseTwo = createAppleDeviceResponse(deviceIdTwo);

        List<Device> devices = List.of(savedDevice, appleDeviceTwo);
        when(deviceRepository.findByBrand(DEVICE_BRAND)).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);
        when(mapper.toResponse(appleDeviceTwo)).thenReturn(responseTwo);

        // When
        List<DeviceResponse> result = deviceService.getByBrand(DEVICE_BRAND);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).brand()).isEqualTo(DEVICE_BRAND);
        assertThat(result.get(1).brand()).isEqualTo(DEVICE_BRAND);
        verify(deviceRepository, times(1)).findByBrand(DEVICE_BRAND);
        verify(mapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    void getByBrand_ShouldCallMapperForEachDevice() {
        // Given
        UUID deviceIdTwo = UUID.randomUUID();
        Device appleDeviceTwo = createAppleDevice(deviceIdTwo);
        List<Device> devices = List.of(savedDevice, appleDeviceTwo);

        when(deviceRepository.findByBrand(DEVICE_BRAND)).thenReturn(devices);
        when(mapper.toResponse(any(Device.class))).thenReturn(expectedResponse);

        // When
        deviceService.getByBrand(DEVICE_BRAND);

        // Then
        verify(mapper, times(2)).toResponse(any(Device.class));
        verify(mapper).toResponse(savedDevice);
        verify(mapper).toResponse(appleDeviceTwo);
    }

    @Test
    void getByState_ShouldReturnEmptyList_WhenNoStateDevicesExist() {
        // Given
        String state = "AVAILABLE";
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(Collections.emptyList());

        // When
        List<DeviceResponse> result = deviceService.getByState(state);

        // Then
        assertThat(result).isEmpty();
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getByState_ShouldReturnMultipleDevices_WhenMultipleStateDevicesExist() {
        // Given
        String state = "AVAILABLE";
        UUID deviceIdTwo = UUID.randomUUID();
        Device deviceTwo = createSecondDevice(deviceIdTwo);
        DeviceResponse responseTwo = createSecondDeviceResponse(deviceIdTwo);

        List<Device> devices = List.of(savedDevice, deviceTwo);
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);
        when(mapper.toResponse(deviceTwo)).thenReturn(responseTwo);

        // When
        List<DeviceResponse> result = deviceService.getByState(state);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).state()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(result.get(1).state()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
        verify(mapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    void getByState_ShouldHandleLowercaseState() {
        // Given
        String state = "available";
        List<Device> devices = List.of(savedDevice);
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        List<DeviceResponse> result = deviceService.getByState(state);

        // Then
        assertThat(result).hasSize(1);
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    void getByState_ShouldHandleMixedCaseState() {
        // Given
        String state = "AvAiLaBlE";
        List<Device> devices = List.of(savedDevice);
        when(deviceRepository.findByState(DeviceState.AVAILABLE)).thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        List<DeviceResponse> result = deviceService.getByState(state);

        // Then
        assertThat(result).hasSize(1);
        verify(deviceRepository, times(1)).findByState(DeviceState.AVAILABLE);
    }

    @Test
    void getByState_ShouldThrowException_WhenInvalidStateProvided() {
        // Given
        String invalidState = "INVALID_STATE";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deviceService.getByState(invalidState);
        });

        verify(deviceRepository, never()).findByState(any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getByState_ShouldCallMapperForEachDevice() {
        // Given
        String state = "IN_USE";
        UUID deviceIdTwo = UUID.randomUUID();
        Device deviceTwo = createThirdDevice(deviceIdTwo);
        List<Device> devices = List.of(deviceTwo);

        when(deviceRepository.findByState(DeviceState.IN_USE)).thenReturn(devices);
        when(mapper.toResponse(any(Device.class))).thenReturn(expectedResponse);

        // When
        deviceService.getByState(state);

        // Then
        verify(mapper, times(1)).toResponse(any(Device.class));
        verify(mapper).toResponse(deviceTwo);
    }

    @Test
    void getByBrandAndState_ShouldReturnMultipleDevices_WhenMultipleDevicesMatch() {
        // Given
        String brand = "Apple";
        String state = "AVAILABLE";
        UUID deviceIdTwo = UUID.randomUUID();
        Device appleDeviceTwo = createAppleDevice(deviceIdTwo);
        DeviceResponse responseTwo = createAppleDeviceResponse(deviceIdTwo);

        List<Device> devices = List.of(savedDevice, appleDeviceTwo);
        when(deviceRepository.findByBrandAndState(brand, DeviceState.AVAILABLE))
                .thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);
        when(mapper.toResponse(appleDeviceTwo)).thenReturn(responseTwo);

        // When
        List<DeviceResponse> result = deviceService.getByBrandAndState(brand, state);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).brand()).isEqualTo(brand);
        assertThat(result.get(0).state()).isEqualTo(DeviceState.AVAILABLE);
        assertThat(result.get(1).brand()).isEqualTo(brand);
        assertThat(result.get(1).state()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository, times(1)).findByBrandAndState(brand, DeviceState.AVAILABLE);
        verify(mapper, times(2)).toResponse(any(Device.class));
    }

    @Test
    void getByBrandAndState_ShouldThrowException_WhenInvalidStateProvided() {
        // Given
        String brand = "Apple";
        String invalidState = "INVALID_STATE";

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            deviceService.getByBrandAndState(brand, invalidState);
        });

        verify(deviceRepository, never()).findByBrandAndState(any(), any());
        verify(mapper, never()).toResponse(any());
    }

    @Test
    void getByBrandAndState_ShouldHandleLowercaseState() {
        // Given
        String brand = "Apple";
        String state = "available";
        List<Device> devices = List.of(savedDevice);
        when(deviceRepository.findByBrandAndState(brand, DeviceState.AVAILABLE))
                .thenReturn(devices);
        when(mapper.toResponse(savedDevice)).thenReturn(expectedResponse);

        // When
        List<DeviceResponse> result = deviceService.getByBrandAndState(brand, state);

        // Then
        assertThat(result).hasSize(1);
        verify(deviceRepository, times(1)).findByBrandAndState(brand, DeviceState.AVAILABLE);
    }

    @Test
    void delete_ShouldDeleteDevice_WhenDeviceExistsAndNotInUse() {
        // Given
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));

        // When
        deviceService.delete(DEVICE_ID);

        // Then
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).delete(savedDevice);
    }

    @Test
    void delete_ShouldThrowDeviceDeletionException_WhenDeviceIsInUse() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));

        // When & Then
        DeviceDeletionException exception = assertThrows(DeviceDeletionException.class, () -> {
            deviceService.delete(DEVICE_ID);
        });

        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        assertThat(exception.getMessage()).isEqualTo("Cannot delete device with ID " + DEVICE_ID + " because its state is IN_USE.");
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldThrowDeviceNotFoundException_WhenDeviceDoesNotExist() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(deviceRepository.findById(nonExistentId)).thenReturn(java.util.Optional.empty());

        // When & Then
        assertThrows(DeviceNotFoundException.class, () -> {
            deviceService.delete(nonExistentId);
        });

        verify(deviceRepository, times(1)).findById(nonExistentId);
        verify(deviceRepository, never()).delete(any());
    }

    @Test
    void delete_ShouldDeleteDevice_WhenDeviceStateIsInactive() {
        // Given
        Device retiredDevice = createSavedDevice();
        retiredDevice.setState(DeviceState.INACTIVE);
        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(retiredDevice));

        // When
        deviceService.delete(DEVICE_ID);

        // Then
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).delete(retiredDevice);
    }

    @Test
    void update_ShouldUpdateDevice_WhenAllFieldsProvidedAndDeviceNotInUse() {
        // Given
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                "iPhone 16 Pro",
                "Apple",
                DeviceState.INACTIVE
        );
        Device updatedDevice = createSavedDevice();
        updatedDevice.setName("iPhone 16 Pro");
        updatedDevice.setState(DeviceState.INACTIVE);
        DeviceResponse updatedResponse = new DeviceResponse(
                DEVICE_ID,
                "iPhone 16 Pro",
                DEVICE_BRAND,
                DeviceState.INACTIVE,
                Instant.now()
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);
        when(mapper.toResponse(updatedDevice)).thenReturn(updatedResponse);

        // When
        DeviceResponse result = deviceService.update(DEVICE_ID, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("iPhone 16 Pro");
        assertThat(result.state()).isEqualTo(DeviceState.INACTIVE);
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(mapper, times(1)).toResponse(updatedDevice);
    }

    @Test
    void update_ShouldThrowException_WhenRequiredFieldsAreMissing() {
        // Given - missing state field
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                "iPhone 16 Pro",
                "Apple",
                null
        );

        // When & Then
        DeviceUpdateValidationException exception = assertThrows(DeviceUpdateValidationException.class, () -> {
            deviceService.update(DEVICE_ID, updateRequest);
        });

        assertThat(exception.getMessage())
                .isEqualTo("PUT request requires 'name', 'brand', and 'state' fields to be present.");
        verify(deviceRepository, never()).findById(any());
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_ShouldThrowException_WhenUpdatingNameOrBrandOfInUseDevice() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                "iPhone 16 Pro", // Different name
                DEVICE_BRAND,
                DeviceState.IN_USE
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));

        // When & Then
        DeviceUpdateValidationException exception = assertThrows(DeviceUpdateValidationException.class, () -> {
            deviceService.update(DEVICE_ID, updateRequest);
        });

        assertThat(exception.getMessage())
                .isEqualTo("Cannot update 'name' for device " + DEVICE_ID + " because its state is IN_USE.");
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void update_ShouldAllowStateUpdate_WhenDeviceIsInUseButNameAndBrandUnchanged() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                DEVICE_NAME, // Same name
                DEVICE_BRAND, // Same brand
                DeviceState.AVAILABLE // Different state
        );
        Device updatedDevice = createSavedDevice();
        updatedDevice.setState(DeviceState.AVAILABLE);
        DeviceResponse updatedResponse = new DeviceResponse(
                DEVICE_ID,
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                Instant.now()
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);
        when(mapper.toResponse(updatedDevice)).thenReturn(updatedResponse);

        // When
        DeviceResponse result = deviceService.update(DEVICE_ID, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).save(any(Device.class));
        verify(mapper, times(1)).toResponse(updatedDevice);
    }

    @Test
    void partialUpdate_ShouldUpdateOnlyProvidedFields_WhenDeviceNotInUse() {
        // Given
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                null,
                "Samsung",
                null
        );
        Device updatedDevice = createSavedDevice();
        updatedDevice.setBrand("Samsung");
        DeviceResponse updatedResponse = new DeviceResponse(
                DEVICE_ID,
                DEVICE_NAME, // unchanged
                "Samsung", // updated
                DEVICE_STATE, // unchanged
                Instant.now()
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(savedDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);
        when(mapper.toResponse(updatedDevice)).thenReturn(updatedResponse);

        // When
        DeviceResponse result = deviceService.partialUpdate(DEVICE_ID, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(DEVICE_NAME); // unchanged
        assertThat(result.brand()).isEqualTo("Samsung"); // updated
        assertThat(result.state()).isEqualTo(DEVICE_STATE); // unchanged
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void partialUpdate_ShouldThrowException_WhenUpdatingNameOfInUseDevice() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                "New Name", // trying to change name
                null,
                null
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));

        // When & Then
        DeviceUpdateValidationException exception = assertThrows(DeviceUpdateValidationException.class, () -> {
            deviceService.partialUpdate(DEVICE_ID, updateRequest);
        });

        assertThat(exception.getMessage())
                .isEqualTo("Cannot update 'name' for device " + DEVICE_ID + " because its state is IN_USE.");
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, never()).save(any());
    }

    @Test
    void partialUpdate_ShouldAllowStateUpdate_WhenDeviceIsInUse() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                null, // not changing name
                null, // not changing brand
                DeviceState.AVAILABLE // only changing state
        );
        Device updatedDevice = createSavedDevice();
        updatedDevice.setState(DeviceState.AVAILABLE);
        DeviceResponse updatedResponse = new DeviceResponse(
                DEVICE_ID,
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.AVAILABLE,
                Instant.now()
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);
        when(mapper.toResponse(updatedDevice)).thenReturn(updatedResponse);

        // When
        DeviceResponse result = deviceService.partialUpdate(DEVICE_ID, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.state()).isEqualTo(DeviceState.AVAILABLE);
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

    @Test
    void partialUpdate_ShouldAllowSameNameAndBrand_WhenDeviceIsInUse() {
        // Given
        Device inUseDevice = createSavedDevice();
        inUseDevice.setState(DeviceState.IN_USE);
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                DEVICE_NAME, // same name
                DEVICE_BRAND, // same brand
                DeviceState.INACTIVE // changing state
        );
        Device updatedDevice = createSavedDevice();
        updatedDevice.setState(DeviceState.INACTIVE);
        DeviceResponse updatedResponse = new DeviceResponse(
                DEVICE_ID,
                DEVICE_NAME,
                DEVICE_BRAND,
                DeviceState.INACTIVE,
                Instant.now()
        );

        when(deviceRepository.findById(DEVICE_ID)).thenReturn(java.util.Optional.of(inUseDevice));
        when(deviceRepository.save(any(Device.class))).thenReturn(updatedDevice);
        when(mapper.toResponse(updatedDevice)).thenReturn(updatedResponse);

        // When
        DeviceResponse result = deviceService.partialUpdate(DEVICE_ID, updateRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(DEVICE_NAME);
        assertThat(result.brand()).isEqualTo(DEVICE_BRAND);
        assertThat(result.state()).isEqualTo(DeviceState.INACTIVE);
        verify(deviceRepository, times(1)).findById(DEVICE_ID);
        verify(deviceRepository, times(1)).save(any(Device.class));
    }

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

    private Device createSecondDevice(UUID deviceIdTwo) {
        Device device = new Device();
        device.setId(deviceIdTwo);
        device.setName("Samsung Galaxy S24");
        device.setBrand("Samsung");
        device.setState(DeviceState.AVAILABLE);
        device.setCreatedAt(Instant.now());
        return device;
    }

    private Device createThirdDevice(UUID deviceIdThree) {
        Device device = new Device();
        device.setId(deviceIdThree);
        device.setName("Google Pixel 8");
        device.setBrand("Google");
        device.setState(DeviceState.IN_USE);
        device.setCreatedAt(Instant.now());
        return device;
    }

    private DeviceResponse createSecondDeviceResponse(UUID deviceTwo) {
        Device device = createSecondDevice(deviceTwo);
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getBrand(),
                device.getState(),
                device.getCreatedAt()
        );
    }

    private Device createAppleDevice(UUID deviceId) {
        Device device = new Device();
        device.setId(deviceId);
        device.setName("iPhone 14");
        device.setBrand(DEVICE_BRAND);
        device.setState(DeviceState.AVAILABLE);
        device.setCreatedAt(Instant.now());
        return device;
    }

    private DeviceResponse createAppleDeviceResponse(UUID deviceId) {
        Device device = createAppleDevice(deviceId);
        return new DeviceResponse(
                device.getId(),
                device.getName(),
                device.getBrand(),
                device.getState(),
                device.getCreatedAt()
        );
    }
}
