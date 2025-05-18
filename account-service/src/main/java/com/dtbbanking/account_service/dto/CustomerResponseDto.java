package com.dtbbanking.account_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponseDto {
    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private LocalDateTime dateOfBirth;
    private LocalDateTime createdAt;
}