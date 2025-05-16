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

@Repository
public interface CustomerRepository extends R2dbcRepository<Customer, UUID> {

    Flux<Customer> findByFirstNameContainingIgnoreCase(String firstName, Pageable pageable);

    Flux<Customer> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Mono<Boolean> existsByPhoneNumberOrNationalId(String phoneNumber, String nationalId);

    @Query("SELECT * FROM tb_customers WHERE " +
            " (COALESCE(first_name, '') || ' ' || COALESCE(other_name, '') || ' ' || COALESCE(last_name, '')) ILIKE '%' || :name || '%'")
    Flux<Customer> searchByFullName(@Param("name") String name, Pageable pageable);

}