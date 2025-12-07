package org.management.devices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DeviceUpdateValidationException extends RuntimeException {
    public DeviceUpdateValidationException(String message) {
        super(message);
    }
}
