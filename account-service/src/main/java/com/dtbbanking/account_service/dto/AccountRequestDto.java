package com.dtbbanking.account_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AccountRequestDto {
//    @NotBlank(message = "IBAN is required")
    private String iban;

    @NotBlank(message = "BIC/SWIFT is required")
    private String bicSwift;

    @NotNull(message = "Customer ID is required")
    private UUID customerId;
}



