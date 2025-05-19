package com.dtbbanking.account_service;

import com.dtbbanking.account_service.controller.AccountController;
import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.AccountResponseDto;
import com.dtbbanking.account_service.dto.UpdateAccountRequestDto;
import com.dtbbanking.account_service.service.AccountService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private AccountService accountService;

    @Mock
    private Validator validator;

    @InjectMocks
    private AccountController accountController;

    private final UUID accountId = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    private AccountRequestDto validRequestDto;
    private AccountResponseDto responseDto;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(accountController).build();

        validRequestDto = new AccountRequestDto("DE89 3704 0044 0532 0130 00", "COBADEFFXXX", customerId);
        responseDto = AccountResponseDto.builder()
                .id(accountId)
                .iban(validRequestDto.getIban())
                .bicSwift(validRequestDto.getBicSwift())
                .customerId(customerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createAccount_shouldReturnOk() {
        when(validator.validate(any(AccountRequestDto.class))).thenReturn(Collections.emptySet());
        when(accountService.createAccount(any())).thenReturn(Mono.just(responseDto));

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequestDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.iban").isEqualTo(responseDto.getIban())
                .jsonPath("$.data.bicSwift").isEqualTo(responseDto.getBicSwift())
                .jsonPath("$.data.customerId").isEqualTo(customerId.toString());
    }


    @Test
    void updateAccount_shouldReturnOk() {
        UpdateAccountRequestDto updateDto = new UpdateAccountRequestDto("DTBKKENA", customerId);

        AccountResponseDto updatedResponseDto = AccountResponseDto.builder()
                .id(accountId)
                .iban(validRequestDto.getIban())
                .bicSwift(updateDto.getBicSwift())
                .customerId(updateDto.getCustomerId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(accountService.updateAccount(eq(accountId), eq(updateDto)))
                .thenReturn(Mono.just(updatedResponseDto));

        webTestClient.put()
                .uri("/api/v1/accounts/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateDto)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(accountId.toString())
                .jsonPath("$.data.bicSwift").isEqualTo(updateDto.getBicSwift())
                .jsonPath("$.data.customerId").isEqualTo(updateDto.getCustomerId().toString());
    }

    @Test
    void getAccount_shouldReturnAccount() {
        when(accountService.getAccountById(accountId)).thenReturn(Mono.just(responseDto));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.id").isEqualTo(accountId.toString());
    }

    @Test
    void deleteAccount_shouldReturnOk() {
        when(accountService.deleteAccount(accountId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data").isEqualTo("Account deleted successfully");
    }

    @Test
    void getAccounts_shouldReturnList() {
        when(accountService.getAccountsByFilters(null, null, null, 0, 10))
                .thenReturn(Flux.just(responseDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/accounts")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].id").isEqualTo(accountId.toString());
    }
}

