package com.dtbbanking.customer_service.repository;

import com.dtbbanking.customer_service.models.Customer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Reactive repository interface for performing CRUD operations on {@link Customer} entities.
 *
 * Uses Spring Data R2DBC for non-blocking access to the customer table.
 */
@Repository
public interface CustomerRepository extends R2dbcRepository<Customer, UUID> {

    /**
     * Finds customers whose creation date falls within the specified range.
     *
     * @param start    The start datetime (inclusive).
     * @param end      The end datetime (inclusive).
     * @param pageable The pagination information.
     * @return A {@link Flux} of customers created between the given dates.
     */
    Flux<Customer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    /**
     * Searches for customers by their full name (first, other, and last names combined).
     *
     * This method performs a case-insensitive match using PostgreSQL's `ILIKE` and COALESCE for null handling.
     *
     * @param name     The name fragment to search for.
     * @param pageable The pagination information.
     * @return A {@link Flux} of customers whose full name matches the given input.
     */
    @Query("SELECT * FROM tb_customers WHERE " +
            " (COALESCE(first_name, '') || ' ' || COALESCE(other_name, '') || ' ' || COALESCE(last_name, '')) ILIKE '%' || :name || '%'")
    Flux<Customer> searchByFullName(@Param("name") String name, Pageable pageable);
}
