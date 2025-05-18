package com.dtbbanking.customer_service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomerRequestDtoValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void testInvalidCustomerRequestDto_missingFirstNameAndLastName() {
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setFirstName("");  // Invalid
        dto.setLastName("");   // Invalid

        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }
}