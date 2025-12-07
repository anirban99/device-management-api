package org.management.devices.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DeviceDeletionException extends RuntimeException {
    public DeviceDeletionException(String message) {
        super(message);
    }
}
