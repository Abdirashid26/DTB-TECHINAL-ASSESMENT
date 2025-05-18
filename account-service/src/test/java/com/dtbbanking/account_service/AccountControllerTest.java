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
                .expectBody(AccountResponseDto.class)
                .value(resp -> assertEquals(responseDto.getIban(), resp.getIban()));
    }

    @Test
    void createAccount_shouldReturnBadRequest_whenValidationFails() {
        Path mockPath = mock(Path.class);
        when(mockPath.toString()).thenReturn("iban");

        ConstraintViolation<AccountRequestDto> mockViolation = mock(ConstraintViolation.class);
        when(mockViolation.getPropertyPath()).thenReturn(mockPath);
        when(mockViolation.getMessage()).thenReturn("IBAN is required");

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<AccountRequestDto>> violations = Set.of(mockViolation);
        when(validator.validate(any(AccountRequestDto.class))).thenReturn(violations);

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequestDto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.iban").isEqualTo("IBAN is required");
    }


    @Test
    void updateAccount_shouldReturnOk() {
        UpdateAccountRequestDto updateDto = new UpdateAccountRequestDto("DTBKKENA", customerId);


        // Prepare a response DTO reflecting the updateDto values
        AccountResponseDto updatedResponseDto = AccountResponseDto.builder()
                .id(accountId)
                .iban(validRequestDto.getIban()) // original IBAN
                .bicSwift(updateDto.getBicSwift()) // updated BIC
                .customerId(updateDto.getCustomerId()) // updated customerId
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
                .expectBody(AccountResponseDto.class)
                .value(resp -> {
                    assertNotNull(resp);
                    assertEquals(accountId, resp.getId());
                    assertEquals(updateDto.getBicSwift(), resp.getBicSwift());
                    assertEquals(updateDto.getCustomerId(), resp.getCustomerId());
                });
    }


    @Test
    void getAccount_shouldReturnAccount() {
        when(accountService.getAccountById(accountId)).thenReturn(Mono.just(responseDto));

        webTestClient.get()
                .uri("/api/v1/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountResponseDto.class)
                .value(resp -> assertEquals(accountId, resp.getId()));
    }

    @Test
    void deleteAccount_shouldReturnOk() {
        when(accountService.deleteAccount(accountId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/accounts/{id}", accountId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
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
                .expectBodyList(AccountResponseDto.class)
                .hasSize(1)
                .value(list -> assertEquals(accountId, list.get(0).getId()));
    }
}
