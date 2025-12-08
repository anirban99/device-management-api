package org.management.devices.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.management.devices.domain.DeviceState;
import org.management.devices.dto.DeviceResponse;
import org.management.devices.dto.DeviceUpdateRequest;
import org.management.devices.repository.DeviceRepository;
import org.management.devices.dto.DeviceCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DeviceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeviceRepository deviceRepository;

    private static final String API_PATH = "/devices";
    private static UUID createdDeviceId;
    private static final String BRAND_X = "BrandX";
    private static final String BRAND_Y = "BrandY";

    @Container
    private static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("testuser")
                    .withPassword("testpass");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Test
    @Order(1)
    @DisplayName("1. POST /devices - Should successfully create a new device and return 201")
    void createDevice_success_returns201() throws Exception {
        DeviceCreateRequest request = new DeviceCreateRequest("Smartphone 1", BRAND_X, DeviceState.AVAILABLE);

        MvcResult result = mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Smartphone 1"))
                .andExpect(jsonPath("$.brand").value(BRAND_X))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();

        String responseJson = result.getResponse().getContentAsString();
        DeviceResponse response = objectMapper.readValue(responseJson, DeviceResponse.class);
        createdDeviceId = response.id();

        assertThat(createdDeviceId).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("2. POST /devices - Should return 400 Bad Request for invalid input (missing name)")
    void createDevice_invalidInput_returns400() throws Exception {
        DeviceCreateRequest request = new DeviceCreateRequest(null, BRAND_Y, DeviceState.AVAILABLE);

        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.name").exists());
    }

    @Test
    @Order(3)
    @DisplayName("3. GET /devices/{id} - Should return the created device and 200 OK")
    void getDeviceById_success_returns200() throws Exception {
        mockMvc.perform(get(API_PATH + "/{id}", createdDeviceId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdDeviceId.toString()))
                .andExpect(jsonPath("$.brand").value(BRAND_X));
    }

    @Test
    @Order(4)
    @DisplayName("4. GET /devices/{id} - Should return 404 Not Found for non-existent ID")
    void getDeviceById_notFound_returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(get(API_PATH + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @Order(5)
    @DisplayName("5. POST /devices - Create a second device (IN_USE, BrandY) for filtering tests")
    void createSecondDevice_forFiltering() throws Exception {
        DeviceCreateRequest request = new DeviceCreateRequest("Tablet 2", BRAND_Y, DeviceState.IN_USE);


        mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(6)
    @DisplayName("6. GET /devices - Should return all 2 created devices")
    void getAllDevices_returnsAll() throws Exception {
        mockMvc.perform(get(API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @Order(7)
    @DisplayName("7. GET /devices?brand=X - Should return 1 device filtered by BrandX")
    void getAllDevices_filterByBrand_returnsFiltered() throws Exception {
        mockMvc.perform(get(API_PATH)
                        .param("brand", BRAND_X))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].brand").value(BRAND_X));
    }

    @Test
    @Order(8)
    @DisplayName("8. GET /devices?state=IN_USE - Should return 1 device filtered by state IN_USE")
    void getAllDevices_filterByState_returnsFiltered() throws Exception {
        mockMvc.perform(get(API_PATH)
                        .param("state", DeviceState.IN_USE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].state").value(DeviceState.IN_USE.toString()));
    }

    @Test
    @Order(9)
    @DisplayName("9. PUT /devices/{id} - Should successfully replace device 1 fully (AVAILABLE device)")
    void putDevice_success_fullReplacement() throws Exception {
        // Fully replace device 1 (Smartphone 1, currently AVAILABLE)
        DeviceUpdateRequest request = new DeviceUpdateRequest(
                "Fully Replaced Device", // New Name
                "NewBrand",             // New Brand
                DeviceState.INACTIVE   // New State
        );

        mockMvc.perform(put(API_PATH + "/{id}", createdDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.brand").value(request.brand()))
                .andExpect(jsonPath("$.state").value(DeviceState.INACTIVE.toString()));
    }

    @Test
    @Order(10)
    @DisplayName("10. PUT /devices/{id} - Should fail (409 CONFLICT) when attempting to change brand of IN_USE device 2")
    void putDevice_inUseBrandChange_returns409() throws Exception {
        DeviceUpdateRequest invalidRequest = new DeviceUpdateRequest(
                "Tablet 2",
                "ILLEGAL_BRAND_CHANGE", // Changing Brand
                DeviceState.IN_USE
        );

        UUID inUseDeviceId = deviceRepository.findAll().stream()
                .filter(d -> d.getState() == DeviceState.IN_USE)
                .findFirst().orElseThrow().getId();

        mockMvc.perform(put(API_PATH + "/{id}", inUseDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format("Cannot update 'brand' for device %s because its state is IN_USE.", inUseDeviceId)
                ));
    }

    @Test
    @Order(11)
    @DisplayName("11. PATCH /devices/{id} - Should successfully update name and state (AVAILABLE device)")
    void patchDevice_success_returns200() throws Exception {
        DeviceUpdateRequest request = new DeviceUpdateRequest("Smartphone V2", null, DeviceState.INACTIVE);

        mockMvc.perform(patch(API_PATH + "/{id}", createdDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Smartphone V2"))
                .andExpect(jsonPath("$.state").value(DeviceState.INACTIVE.toString()));
    }

    @Test
    @Order(12)
    @DisplayName("12. PATCH /devices/{id} - Should fail (409 CONFLICT) when updating Brand of IN_USE device")
    void patchDevice_inUseBrandUpdate_returns409() throws Exception {
        DeviceUpdateRequest invalidRequest = new DeviceUpdateRequest(null, "NewBrand", null);

        UUID inUseDeviceId = deviceRepository.findAll().stream()
                .filter(d -> d.getState() == DeviceState.IN_USE)
                .findFirst().orElseThrow().getId();

        mockMvc.perform(patch(API_PATH + "/{id}", inUseDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format("Cannot update 'brand' for device %s because its state is IN_USE.", inUseDeviceId)
                ));
    }

    @Test
    @Order(13)
    @DisplayName("13. PATCH /devices/{id} - Should successfully update State of IN_USE device")
    void patchDevice_inUseStateUpdate_success() throws Exception {
        DeviceUpdateRequest validRequest = new DeviceUpdateRequest(null, null, DeviceState.AVAILABLE);

        UUID inUseDeviceId = deviceRepository.findAll().stream()
                .filter(d -> d.getBrand().equals(BRAND_Y))
                .findFirst().orElseThrow().getId();

        mockMvc.perform(patch(API_PATH + "/{id}", inUseDeviceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value(DeviceState.AVAILABLE.toString()));
    }

    @Test
    @Order(14)
    @DisplayName("14. DELETE /devices/{id} - Should successfully delete an AVAILABLE device and return 204")
    void deleteDevice_available_success_returns204() throws Exception {
        mockMvc.perform(delete(API_PATH + "/{id}", createdDeviceId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(API_PATH + "/{id}", createdDeviceId))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15)
    @DisplayName("15. DELETE /devices/{id} - Should fail (409 CONFLICT) when deleting an IN_USE device")
    void deleteDevice_inUse_returns409() throws Exception {
        DeviceCreateRequest request = new DeviceCreateRequest("Locked Phone", "LockBrand", DeviceState.IN_USE);
        MvcResult result = mockMvc.perform(post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        DeviceResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), DeviceResponse.class);
        UUID lockedDeviceId = response.id();

        mockMvc.perform(delete(API_PATH + "/{id}", lockedDeviceId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format("Cannot delete device with ID %s because its state is IN_USE.", lockedDeviceId)
                ));
    }

    @Test
    @Order(16)
    @DisplayName("16. DELETE /devices/{id} - Should return 404 Not Found for non-existent ID")
    void deleteDevice_notFound_returns404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();

        mockMvc.perform(delete(API_PATH + "/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}
