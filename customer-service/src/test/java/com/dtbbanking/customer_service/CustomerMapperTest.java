package com.dtbbanking.customer_service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.mapper.CustomerMapper;
import com.dtbbanking.customer_service.models.Customer;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerMapperTest {

    @Test
    public void testToEntity_shouldMapDtoToEntityCorrectly() {
        // Given
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setOtherName("Smith");

        // When
        Customer entity = CustomerMapper.toEntity(dto);

        // Then
        assertEquals("John", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertEquals("Smith", entity.getOtherName());
        assertNotNull(entity.getCreatedAt());
        assertNotNull(entity.getUpdatedAt());
    }

    @Test
    public void testToResponseDto_shouldMapEntityToDtoCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Jane");
        customer.setLastName("Doe");
        customer.setOtherName("Ann");
        customer.setCreatedAt(createdAt);

        // When
        CustomerResponseDto responseDto = CustomerMapper.toResponseDto(customer);

        // Then
        assertEquals(id, responseDto.getId());
        assertEquals("Jane Doe Ann", responseDto.getFullName());
        assertEquals(createdAt, responseDto.getCreatedAt());
    }

    @Test
    public void testToResponseDto_shouldHandleNullOptionalFields() {
        // Given
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Customer customer = new Customer();
        customer.setId(id);
        customer.setFirstName("Tom");
        customer.setLastName("Hardy");
        customer.setCreatedAt(createdAt);

        // When
        CustomerResponseDto responseDto = CustomerMapper.toResponseDto(customer);

        // Then
        assertEquals("Tom Hardy", responseDto.getFullName());
    }
}