package org.management.devices.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.management.devices.domain.DeviceState;
import java.util.Arrays;

public class ValidDeviceStateValidator implements ConstraintValidator<ValidDeviceState, DeviceState> {
    private boolean allowNull;

    @Override
    public void initialize(ValidDeviceState annotation) {
        this.allowNull = annotation.allowNull();
    }

    @Override
    public boolean isValid(DeviceState value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }
        return Arrays.asList(DeviceState.values()).contains(value);
    }
}
