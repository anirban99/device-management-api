package org.management.devices.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Error response structure")
public record ErrorResponse(
        @Schema(description = "Timestamp when the error occurred", example = "2024-12-08T10:30:00Z", type = "string", format = "date-time")
        Instant timestamp,
        @Schema(description = "HTTP status code",  examples = {"400", "404", "409", "500"}, minimum = "100", maximum = "599")
        int status,
        @Schema(description = "HTTP status reason phrase", example = "Bad Request, Not Found, Conflict, Internal Server Error")
        String error,
        @Schema(description = "Detailed error message explaining what went wrong", example = "Cannot delete device with ID 123e4567-e89b-12d3-a456-426614174000 because its state is IN_USE.")
        String message,
        @Schema(description = "Additional validation error details (field-level errors)", example = "{\"name\": \"must not be blank\", \"brand\": \"must not be null\"}", nullable = true)
        Map<String, String> details
) {
    public ErrorResponse(Instant timestamp, int status, String error, String message) {
        this(timestamp, status, error, message, null);
    }
}
