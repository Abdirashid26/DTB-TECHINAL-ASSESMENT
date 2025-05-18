package com.dtbbanking.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {

    private UUID id;
    private String iban;
    private String bicSwift;
    private UUID customerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}