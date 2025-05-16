package com.dtbbanking.customer_service.mapper;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.models.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerMapper {

    public static Customer toEntity(CustomerRequestDto dto) {
        return Customer.builder()
//                .id(UUID.randomUUID())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .otherName(dto.getOtherName())
                .phoneNumber(dto.getPhoneNumber())
                .nationalId(dto.getNationalId())
                .dateOfBirth(dto.getDateOfBirth())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static CustomerResponseDto toResponseDto(Customer entity) {
        return CustomerResponseDto.builder()
                .id(entity.getId())
                .fullName(entity.getFirstName() + " " + entity.getLastName())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .nationalId(entity.getNationalId())
                .dateOfBirth(entity.getDateOfBirth())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}