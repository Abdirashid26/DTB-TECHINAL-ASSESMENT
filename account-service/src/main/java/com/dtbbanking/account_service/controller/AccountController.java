package com.dtbbanking.account_service.controller;

import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.AccountResponseDto;
import com.dtbbanking.account_service.dto.UpdateAccountRequestDto;
import com.dtbbanking.account_service.service.AccountService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing bank accounts.
 * Supports account creation, update, retrieval, filtering, and deletion.
 */
@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final Validator validator;

    /**
     * Creates a new account for an existing customer.
     *
     * @param dto the account request payload
     * @return 200 OK with the created account details, or 400 Bad Request with validation errors
     */
    @PostMapping
    public Mono<ResponseEntity<?>> createAccount(@RequestBody @Valid AccountRequestDto dto) {
        log.info("Creating account for customerId: {}", dto.getCustomerId());
        Set<ConstraintViolation<AccountRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            return Mono.just(ResponseEntity.badRequest().body(errors));
        }
        return accountService.createAccount(dto)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an existing account.
     *
     * @param id  the account ID to update
     * @param dto the updated account data
     * @return 200 OK with the updated account details, or 400 Bad Request with validation errors
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<?>> updateAccount(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateAccountRequestDto dto) {
        log.info("Updating account with id: {}", id);
        return accountService.updateAccount(id, dto)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a specific account by its ID.
     *
     * @param id the account ID
     * @return 200 OK with the account details, or 404 Not Found if not found
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<AccountResponseDto>> getAccount(@PathVariable UUID id) {
        log.info("Getting account with id: {}", id);
        return accountService.getAccountById(id)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a list of accounts, optionally filtered by IBAN, BIC/SWIFT, or card alias.
     *
     * @param iban      optional IBAN filter
     * @param bicSwift  optional BIC/SWIFT filter
     * @param cardAlias optional card alias filter
     * @param page      page number (default is 0)
     * @param size      page size (default is 10)
     * @return list of matching accounts
     */
    @GetMapping
    public Flux<AccountResponseDto> getAccounts(
            @RequestParam(required = false) String iban,
            @RequestParam(required = false) String bicSwift,
            @RequestParam(required = false) String cardAlias,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Retrieving accounts with filters");
        return accountService.getAccountsByFilters(iban, bicSwift, cardAlias, page, size);
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id the account ID
     * @return 200 OK when deletion is successful
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAccount(@PathVariable UUID id) {
        log.info("Deleting account with id: {}", id);
        return accountService.deleteAccount(id)
                .thenReturn(ResponseEntity.ok().build());
    }
}


