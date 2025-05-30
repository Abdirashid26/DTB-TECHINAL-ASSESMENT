package com.dtbbanking.customer_service.mapper;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.models.Customer;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for mapping between Customer-related DTOs and entities.
 *
 * Provides conversion logic between {@link CustomerRequestDto}, {@link CustomerResponseDto},
 * and the {@link Customer} entity.
 */
public class CustomerMapper {

    /**
     * Converts a {@link CustomerRequestDto} into a {@link Customer} entity.
     *
     * Sets the current timestamp for both `createdAt` and `updatedAt` fields.
     *
     * @param dto The DTO containing customer input data.
     * @return A new {@link Customer} entity populated from the DTO.
     */
    public static Customer toEntity(CustomerRequestDto dto) {
        return Customer.builder()
//                .id(UUID.randomUUID()) // ID is usually generated by the persistence layer
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .otherName(dto.getOtherName())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts a {@link Customer} entity into a {@link CustomerResponseDto}.
     *
     * Builds the full name by concatenating first, last, and other names (ignoring nulls).
     *
     * @param entity The entity to convert.
     * @return A {@link CustomerResponseDto} with mapped fields from the entity.
     */
    public static CustomerResponseDto toResponseDto(Customer entity) {
        return CustomerResponseDto.builder()
                .id(entity.getId())
                .fullName(Stream.of(entity.getFirstName(), entity.getLastName(), entity.getOtherName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(" ")))
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
