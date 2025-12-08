package org.management.devices.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidDeviceStateValidator.class)
public @interface ValidDeviceState {
    String message() default "Invalid device state. Valid values are: AVAILABLE, IN_USE, INACTIVE";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean allowNull() default false;
}
