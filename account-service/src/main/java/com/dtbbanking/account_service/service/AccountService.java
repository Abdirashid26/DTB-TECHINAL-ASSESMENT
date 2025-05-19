package com.dtbbanking.account_service.service;

import com.dtbbanking.account_service.dto.AccountRequestDto;
import com.dtbbanking.account_service.dto.AccountResponseDto;
import com.dtbbanking.account_service.dto.UpdateAccountRequestDto;
import com.dtbbanking.account_service.errors.CustomerNotFoundException;
import com.dtbbanking.account_service.errors.DuplicateResourceException;
import com.dtbbanking.account_service.mappers.AccountMapper;
import com.dtbbanking.account_service.models.Account;
import com.dtbbanking.account_service.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service class responsible for handling account-related operations,
 * including creation, retrieval, update, deletion, and filtering of accounts.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;
    private final CardClient cardClient;

    /**
     * Creates a new account after validating that the customer exists and the IBAN is unique.
     *
     * @param dto the account request DTO
     * @return Mono emitting the created account response DTO
     */
    public Mono<AccountResponseDto> createAccount(AccountRequestDto dto) {
        return customerClient.existsById(dto.getCustomerId())
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new CustomerNotFoundException("Customer not found"));
                    }

                    // Generate IBAN here
                    String generatedIban = generateIban();

                    return accountRepository.existsByIban(generatedIban)
                            .flatMap(ibanExists -> {
                                if (ibanExists) {
                                    return Mono.error(new DuplicateResourceException("IBAN already exists"));
                                }

                                // Build Account entity with generated IBAN
                                Account account = Account.builder()
                                        .iban(generatedIban)
                                        .bicSwift(dto.getBicSwift())
                                        .customerId(dto.getCustomerId())
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build();

                                log.info("Creating account for customer: {} with generated IBAN: {}", dto.getCustomerId(), generatedIban);

                                return accountRepository.save(account)
                                        .doOnSuccess(saved -> log.info("Account created with ID: {}", saved.getId()))
                                        .map(AccountMapper::toResponseDto);
                            });
                });
    }

    /**
     * Simple IBAN generator example (replace with real logic).
     */
    private String generateIban() {
        String countryCode = "KE";
        int checkDigits = ThreadLocalRandom.current().nextInt(10, 99);
        // Generate 18 alphanumeric chars for BBAN
        String bban = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 18).toUpperCase();
        return countryCode + checkDigits + bban;
    }


    /**
     * Retrieves an account by its ID.
     *
     * @param id the account UUID
     * @return Mono emitting the account response DTO, or error if not found
     */
    public Mono<AccountResponseDto> getAccountById(UUID id) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Account not found")))
                .map(AccountMapper::toResponseDto);
    }

    /**
     * Retrieves a list of accounts based on optional filters like IBAN, BIC/SWIFT, or card alias.
     * Supports pagination using page and size.
     *
     * @param iban      optional IBAN filter
     * @param bicSwift  optional BIC/SWIFT filter
     * @param cardAlias optional card alias filter
     * @param page      page number (zero-based)
     * @param size      number of records per page
     * @return Flux emitting account response DTOs
     */
    public Flux<AccountResponseDto> getAccountsByFilters(String iban, String bicSwift, String cardAlias, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        if (iban != null && !iban.isEmpty()) {
            log.info("Filtering accounts by IBAN: {}", iban);
            return accountRepository.findByIbanContainingIgnoreCase(iban, pageable)
                    .map(AccountMapper::toResponseDto);
        }

        if (bicSwift != null && !bicSwift.isEmpty()) {
            log.info("Filtering accounts by BIC/SWIFT: {}", bicSwift);
            return accountRepository.findByBicSwiftContainingIgnoreCase(bicSwift, pageable)
                    .map(AccountMapper::toResponseDto);
        }

        if (cardAlias != null && !cardAlias.isEmpty()) {
            log.info("Filtering accounts by card alias: {}", cardAlias);
            return cardClient.getAccountIdsByCardAlias(cardAlias)
                    .flatMapMany(accountIds -> accountRepository.findByIdIn(accountIds)
                            .skip((long) page * size)
                            .take(size)
                            .map(AccountMapper::toResponseDto)
                    );
        }

        log.info("No filters applied, returning all accounts");
        return accountRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(AccountMapper::toResponseDto);
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id the UUID of the account
     * @return Mono signaling completion, or error if account not found
     */
    public Mono<Void> deleteAccount(UUID id) {
        return accountRepository.existsById(id)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new CustomerNotFoundException("Account not found"));
                    }
                    return accountRepository.deleteById(id);
                });
    }

    /**
     * Updates an existing account's BIC/SWIFT and customer ID if provided.
     *
     * @param id  the UUID of the account to update
     * @param dto the update request containing optional bicSwift and customerId
     * @return Mono emitting the updated account response DTO
     */
    public Mono<AccountResponseDto> updateAccount(UUID id, UpdateAccountRequestDto dto) {
        return accountRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomerNotFoundException("Account not found")))
                .flatMap(existing -> {
                    Mono<Void> validateCustomer = Mono.empty();

                    if (dto.getCustomerId() != null && !dto.getCustomerId().equals(existing.getCustomerId())) {
                        validateCustomer = customerClient.existsById(dto.getCustomerId())
                                .flatMap(exists -> {
                                    if (!exists) {
                                        return Mono.error(new CustomerNotFoundException("Customer not found"));
                                    }
                                    return Mono.fromRunnable(() -> existing.setCustomerId(dto.getCustomerId()));
                                });
                    }

                    if (dto.getBicSwift() != null && !dto.getBicSwift().isBlank()) {
                        existing.setBicSwift(dto.getBicSwift().trim());
                    }

                    existing.setUpdatedAt(LocalDateTime.now());

                    return validateCustomer.then(accountRepository.save(existing));
                })
                .map(AccountMapper::toResponseDto);
    }

}
