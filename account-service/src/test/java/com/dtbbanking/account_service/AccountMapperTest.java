package com.dtbbanking.account_service;

import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.AccountResponseDto;
import com.dtbbanking.account_service.mappers.AccountMapper;
import com.dtbbanking.account_service.models.Account;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class AccountMapperTest {

    @Test
    void toEntity_shouldMapFieldsCorrectly() {
        UUID customerId = UUID.randomUUID();
        AccountRequestDto dto = new AccountRequestDto("KE12...", "BIC123", customerId);

        Account account = AccountMapper.toEntity(dto);

        assertNotNull(account);
        assertEquals(dto.getIban(), account.getIban());
        assertEquals(dto.getBicSwift(), account.getBicSwift());
        assertEquals(dto.getCustomerId(), account.getCustomerId());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());

        // createdAt and updatedAt should be close to now
        LocalDateTime now = LocalDateTime.now();
        assertTrue(!account.getCreatedAt().isAfter(now));
        assertTrue(!account.getUpdatedAt().isAfter(now));
    }

    @Test
    void toResponseDto_shouldMapFieldsCorrectly() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        Account account = Account.builder()
                .id(id)
                .iban("KE34...")
                .bicSwift("BIC456")
                .customerId(customerId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        AccountResponseDto responseDto = AccountMapper.toResponseDto(account);

        assertNotNull(responseDto);
        assertEquals(account.getId(), responseDto.getId());
        assertEquals(account.getIban(), responseDto.getIban());
        assertEquals(account.getBicSwift(), responseDto.getBicSwift());
        assertEquals(account.getCustomerId(), responseDto.getCustomerId());
        assertEquals(account.getCreatedAt(), responseDto.getCreatedAt());
        assertEquals(account.getUpdatedAt(), responseDto.getUpdatedAt());
    }
}
