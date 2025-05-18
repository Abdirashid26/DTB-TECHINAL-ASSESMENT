package com.dtbbanking.account_service.mappers;

import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.AccountResponseDto;
import com.dtbbanking.account_service.models.Account;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class to map between Account entity and DTOs.
 */
public class AccountMapper {

    /**
     * Converts AccountRequestDto to Account entity.
     *
     * @param dto the AccountRequestDto containing account data.
     * @return Account entity with fields populated from dto.
     *         The createdAt and updatedAt fields are set to the current time.
     */
    public static Account toEntity(AccountRequestDto dto) {
        return Account.builder()
                .iban(dto.getIban())
                .bicSwift(dto.getBicSwift())
                .customerId(dto.getCustomerId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Converts Account entity to AccountResponseDto.
     *
     * @param account the Account entity to convert.
     * @return AccountResponseDto with fields copied from the entity.
     */
    public static AccountResponseDto toResponseDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .iban(account.getIban())
                .bicSwift(account.getBicSwift())
                .customerId(account.getCustomerId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
