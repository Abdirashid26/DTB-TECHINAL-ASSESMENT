package com.dtbbanking.account_service.repository;

import com.dtbbanking.account_service.models.Account;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
/**
 * Reactive repository interface for Account entities,
 * providing methods for CRUD operations and custom queries.
 */
public interface AccountRepository extends ReactiveCrudRepository<Account, UUID> {

    /**
     * Finds accounts where the IBAN contains the specified string (case-insensitive),
     * with pagination support.
     *
     * @param iban     the partial IBAN to search for
     * @param pageable pagination information (page number, size, sorting)
     * @return a Flux stream of matching Account entities
     */
    Flux<Account> findByIbanContainingIgnoreCase(String iban, Pageable pageable);

    /**
     * Finds accounts where the BIC/SWIFT contains the specified string (case-insensitive),
     * with pagination support.
     *
     * @param bicSwift the partial BIC/SWIFT to search for
     * @param pageable pagination information (page number, size, sorting)
     * @return a Flux stream of matching Account entities
     */
    Flux<Account> findByBicSwiftContainingIgnoreCase(String bicSwift, Pageable pageable);

    /**
     * Checks if an account exists with the exact specified IBAN.
     *
     * @param iban the IBAN to check for existence
     * @return a Mono emitting true if an account with the IBAN exists, false otherwise
     */
    Mono<Boolean> existsByIban(String iban);

    /**
     * Finds accounts whose IDs are included in the provided list.
     *
     * @param ids the list of account UUIDs to find
     * @return a Flux stream of matching Account entities
     */
    Flux<Account> findByIdIn(List<UUID> ids);
}
