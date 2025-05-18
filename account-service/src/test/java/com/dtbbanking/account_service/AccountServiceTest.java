package com.dtbbanking.account_service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.UpdateAccountRequestDto;
import com.dtbbanking.account_service.errors.CustomerNotFoundException;
import com.dtbbanking.account_service.errors.DuplicateResourceException;
import com.dtbbanking.account_service.models.Account;
import com.dtbbanking.account_service.repository.AccountRepository;
import com.dtbbanking.account_service.service.AccountService;
import com.dtbbanking.account_service.service.CardClient;
import com.dtbbanking.account_service.service.CustomerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class AccountServiceTest {

    private AccountRepository accountRepository;
    private CustomerClient customerClient;
    private CardClient cardClient;
    private AccountService accountService;

    private Account savedAccount;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepository.class);
        customerClient = mock(CustomerClient.class);
        cardClient = mock(CardClient.class);
        accountService = new AccountService(accountRepository, customerClient,cardClient);

        savedAccount = Account.builder()
                .id(UUID.randomUUID())
                .iban("KE12345678901234567890")
                .bicSwift("NWBKGB2L")
                .customerId(UUID.randomUUID())
                .build();
    }

    @Test
    void testCreateAccountSuccess() {
        // Create DTO without IBAN since it will be generated in the service
        AccountRequestDto dto = new AccountRequestDto(null, "NWBKKE2L", savedAccount.getCustomerId());

        when(customerClient.existsById(dto.getCustomerId())).thenReturn(Mono.just(true));
        when(accountRepository.existsByIban(anyString())).thenReturn(Mono.just(false));

        // Capture Account entity passed to save
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        when(accountRepository.save(accountCaptor.capture())).thenReturn(Mono.just(savedAccount));

        StepVerifier.create(accountService.createAccount(dto))
                .assertNext(response -> {
                    // Assert customerId matches
                    assertEquals(dto.getCustomerId(), response.getCustomerId());
                    // Assert IBAN is not null and starts with "KE"
                    assertNotNull(response.getIban());
                    assertTrue(response.getIban().startsWith("KE"));
                })
                .verifyComplete();

        verify(customerClient).existsById(dto.getCustomerId());
        verify(accountRepository).existsByIban(anyString());
        verify(accountRepository).save(any(Account.class));

        // Additional check: IBAN passed to save is generated and starts with "KE"
        Account savedAccountArg = accountCaptor.getValue();
        assertNotNull(savedAccountArg.getIban());
        assertTrue(savedAccountArg.getIban().startsWith("KE"));
    }


    @Test
    void testCreateAccountCustomerNotExists() {
        AccountRequestDto dto = new AccountRequestDto("KE12345678901234567890", "NWBKGB2L", savedAccount.getCustomerId());

        when(customerClient.existsById(dto.getCustomerId())).thenReturn(Mono.just(false));

        StepVerifier.create(accountService.createAccount(dto))
                .expectErrorMatches(e -> e instanceof CustomerNotFoundException &&
                        e.getMessage().equals("Customer not found"))
                .verify();

        verify(customerClient).existsById(dto.getCustomerId());
        verifyNoMoreInteractions(accountRepository);
    }



    @Test
    void testGetAccountByIdFound() {
        when(accountRepository.findById(savedAccount.getId())).thenReturn(Mono.just(savedAccount));

        StepVerifier.create(accountService.getAccountById(savedAccount.getId()))
                .expectNextMatches(response -> response.getIban().equals(savedAccount.getIban()))
                .verifyComplete();
    }

    @Test
    void testGetAccountByIdNotFound() {
        UUID randomId = UUID.randomUUID();
        when(accountRepository.findById(randomId)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.getAccountById(randomId))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void testDeleteAccountFound() {
        UUID id = savedAccount.getId();
        when(accountRepository.existsById(id)).thenReturn(Mono.just(true));
        when(accountRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(accountService.deleteAccount(id))
                .verifyComplete();
    }

    @Test
    void testDeleteAccountNotFound() {
        UUID id = UUID.randomUUID();
        when(accountRepository.existsById(id)).thenReturn(Mono.just(false));

        StepVerifier.create(accountService.deleteAccount(id))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @Test
    void testGetAccountsByIbanFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByIbanContainingIgnoreCase("GB29", pageable))
                .thenReturn(Flux.just(savedAccount));

        StepVerifier.create(accountService.getAccountsByFilters("GB29", null, null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetAccountsByBicSwiftFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(accountRepository.findByBicSwiftContainingIgnoreCase("NWBK", pageable))
                .thenReturn(Flux.just(savedAccount));

        StepVerifier.create(accountService.getAccountsByFilters(null, "NWBK", null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGetAccountsNoFilter() {
        when(accountRepository.findAll()).thenReturn(Flux.just(savedAccount));

        StepVerifier.create(accountService.getAccountsByFilters(null, null, null, 0, 10))
                .expectNextCount(1)
                .verifyComplete();
    }


    @Test
    void testUpdateAccount_BicSwiftAndCustomerIdUpdatedSuccessfully() {
        UUID accountId = UUID.randomUUID();
        UUID oldCustomerId = UUID.randomUUID();
        UUID newCustomerId = UUID.randomUUID();

        Account existing = Account.builder()
                .id(accountId)
                .iban("KE40DTB00123456789")
                .bicSwift("OLDKENA")
                .customerId(oldCustomerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UpdateAccountRequestDto updateDto = new UpdateAccountRequestDto("DTBKKENA", newCustomerId);

        when(accountRepository.findById(accountId)).thenReturn(Mono.just(existing));
        when(customerClient.existsById(newCustomerId)).thenReturn(Mono.just(true));
        when(accountRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(accountService.updateAccount(accountId, updateDto))
                .expectNextMatches(response ->
                        response.getBicSwift().equals("DTBKKENA") &&
                                response.getCustomerId().equals(newCustomerId))
                .verifyComplete();
    }


}