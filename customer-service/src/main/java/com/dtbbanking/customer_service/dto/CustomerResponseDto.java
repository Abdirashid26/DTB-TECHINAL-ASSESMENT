package com.dtbbanking.customer_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CustomerResponseDto {
    private UUID id;
    private String fullName;
    private LocalDateTime createdAt;
}