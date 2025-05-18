package com.dtbbanking.account_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UpdateAccountRequestDto {
    

    private String bicSwift;

    private UUID customerId;
}