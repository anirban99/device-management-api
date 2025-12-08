# Device Management API

## Project Overview

This project implements a **REST API** for managing device resources, fulfilling the requirements of the backend challenge. It is built using **Spring Boot 3** and leverages **PostgreSQL** for persistence, running entirely within **Docker containers**.

The API is designed with a focus on granular commits, test coverage, best practices (e.g., DTOs, Service/Repository layers, Global Exception Handling), and robust domain validation.

### Requirements & Tech Stack

| Feature | Status            | Details |
| :--- |:------------------| :--- |
| **Language** | Java 21           | Built using OpenJDK 21. |
| **Framework** | Spring Boot 3.3.x | Starter Web, Data JPA, Validation. |
| **Database** | PostgreSQL        | Non-in-memory database enforced via Testcontainers and Docker Compose. |
| **Containerization**| Docker            | Uses a multi-stage `Dockerfile` and `docker-compose.yml`. |
| **Testing** | Integration/Unit  | `MockMvc` and `Testcontainers` are used for robust testing. |

***

## **Architecture**

The project follows a layered structure:

    src/main/java/com/example/devices
     ├── controller       → REST API layer
     ├── service          → Business logic + validation rules
     ├── domain           → JPA entities + enums
     ├── repository       → Spring Data JPA repositories
     ├── dto              → Request/response payloads
     ├── mapper           → MapStruct mappers
     ├── exception        → Global exception handling
     └── DevicesApiApplication.java

## Getting Started

These instructions cover how to build, run, and interact with the application locally using Docker Compose.

### Prerequisites

* **Docker** and **Docker Compose** installed (or Docker Desktop).
* **Maven 3.9+** (only required to run unit tests directly on the host).
* **Java 21+** (only required to run unit tests directly on the host).

### **Clone the repository**

The repository can be found here: https://github.com/anirban99/device-management-api#

``` sh
git clone git@github.com:anirban99/device-management-api.git
cd devices-api
```

##  **Build Instructions**

### **Running with Docker Compose**

This will start:

-   PostgreSQL
-   The API container

### 1. Build and Run the Application

The `docker-compose.yml` file handles starting both the API and the PostgreSQL database.

```bash
# 1. Build the Java application image and start all services (in detached mode)
docker-compose up --build -d

# 2. Wait for a few moments for PostgreSQL and the Spring Boot app to initialize (check logs if needed)
docker-compose logs devices-api

# 3. View running containers
docker-compose ps
```

API will run at: **http://localhost:8080/devices**

### 2. Stop and Clean Up
To stop the running containers and remove the network:

```bash
# Stop containers and remove the network
docker-compose down

# To remove database data as well (CAUTION: Deletes all persisted data)
docker-compose down --volumes
```

## API Documentation & Endpoints

Once the application is running, API documentation can be accessed at: **http://localhost:8080/swagger-ui/index.html**

| Field | Type      | Constraints | Notes |
| :--- |:----------| :--- | :--- |
| **id** | UUID      | Primary Key | Auto-generated.
| **name** | String    | Not Null |
| **brand** | String    | Not Null |
| **state**| Enum      | AVAILABLE, IN_USE, INACTIVE | Defaults to AVAILABLE on creation.
| **creationTime** | Instant   | Immutable | Auto-set on creation; cannot be updated.

***

## **API Endpoints Overview**

### **POST /devices**

Create a new device. Name and Brand required.

### **GET /devices/{id}**

Fetch a single device.

### **GET /devices**

Fetch all devices, optional filtering.

### Query params:

-   `brand`
-   `state`

### **PUT /devices/{id}**

Fully replace a device. Name/Brand update restricted if `IN_USE`.

### **PATCH /devices/{id}**

Partially update a device. (only provided fields are updated). Name/Brand update restricted if `IN_USE`.

### **DELETE /devices/{id}**

Delete a single device. Cannot delete if device is `IN_USE`.


## **Business Rule Enforcement**

| Scenario                                   | Behavior |
|:-------------------------------------------|:----------|
| **Update `name` when device is `IN_USE`**  | 400 Bad Request
| **Update `brand` when device is `IN_USE`** | 400 Bad Request
| **Delete a device in `IN_USE`**            | 400 Bad Request
| **Modify `createdAt`**                     | Ignored (never updated)

## **Testing**
The project includes both Unit Tests and Integration Tests to ensure reasonable test coverage.

### Unit Tests
Unit tests are focused on the business logic within the DeviceService layer, using Mockito to mock the repository.

```bash
mvn test
```

### Integration Tests
Integration tests are crucial for verifying the full stack, including persistence, API routing, and exception handling. They use Testcontainers to spin up an ephemeral PostgreSQL instance for testing.

```bash
mvn clean verify
```

## **Best Practices**

The solution was built following several best practices to ensure quality and maintainability:

**Layered Architecture:** Clear separation of Controller, Service, and Repository layers.

**DTO Pattern:** Usage of distinct Request (DeviceCreateRequest, DeviceUpdateRequest) and Response (DeviceResponse) DTOs to decouple the API contract from the JPA entity.

**Global Exception Handling:** A @ControllerAdvice maps custom exceptions (DeviceNotFoundException, DeviceUpdateValidationException, etc.) to specific HTTP status codes (404, 409, 400) and a consistent JSON error format.

**Immutability Enforcement:** The creationTime field is made immutable using the JPA annotation @Column(updatable = false).

**Flyway:** Integrated a database migration tool (Flyway) to manage schema changes reliably instead of relying solely on Hibernate's ddl-auto: update.

**Granular Commits:** All features were implemented through small, atomic commits with detailed messages explaining the changes and their corresponding requirements.

**Containerization:** Utilizes a multi-stage Dockerfile for a small, secure runtime image, and docker-compose for easy orchestration.

**API Documentation:** Integrated Springdoc OpenAPI(Swagger) to generate interactive API documentation automatically.

## **Future Improvements**

**Pagination/Sorting:** Enhance the GET /devices endpoint to support pagination (page, size) and dynamic sorting.

**Performance:** Introduce caching (e.g., using Redis) for frequently accessed, immutable resources like device lookup by ID.