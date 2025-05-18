package com.dtbbanking.customer_service.controller;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.dto.UpdateCustomerRequestDto;
import com.dtbbanking.customer_service.service.CustomerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing Customer resources.
 *
 * Provides endpoints for creating, updating, retrieving, and deleting customers.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final Validator validator;  // javax.validation.Validator

    /**
     * Creates a new customer.
     *
     * @param dto The request DTO containing customer data.
     * @return A {@link Mono} emitting the response entity containing the created customer or validation errors.
     */
    @PostMapping
    public Mono<ResponseEntity<?>> createCustomer(@RequestBody CustomerRequestDto dto) {
        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            return Mono.just(ResponseEntity.badRequest().body(errors));
        }
        return customerService.createCustomer(dto)
                .map(ResponseEntity::ok);
    }

    /**
     * Updates an existing customer.
     *
     * @param id  The UUID of the customer to update.
     * @param dto The DTO containing updated customer information.
     * @return A {@link Mono} emitting the updated {@link CustomerResponseDto}.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<CustomerResponseDto>> updateCustomer(
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequestDto dto) {
        return customerService.updateCustomer(id, dto)
                .map(ResponseEntity::ok);
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param id The UUID of the customer.
     * @return A {@link Mono} emitting the {@link CustomerResponseDto}.
     */
    @GetMapping("/{id}")
    public Mono<CustomerResponseDto> getCustomer(@PathVariable UUID id) {
        log.info("Received request to fetch customer with ID: {}", id);
        return customerService.getCustomerById(id);
    }

    /**
     * Retrieves customers with optional filtering.
     *
     * @param name  Optional name filter.
     * @param start Optional start datetime for creation date filtering.
     * @param end   Optional end datetime for creation date filtering.
     * @param page  Page number for pagination (default is 0).
     * @param size  Page size for pagination (default is 10).
     * @return A {@link Flux} emitting matching {@link CustomerResponseDto} objects.
     */
    @GetMapping
    public Flux<CustomerResponseDto> getCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (name != null) {
            log.info("Filtering customers by name='{}', page={}, size={}", name, page, size);
            return customerService.getCustomersByFirstName(name, page, size);
        }

        if (start != null && end != null) {
            log.info("Filtering customers by creation date between {} and {}", start, end);
            return customerService.getCustomersByCreatedDate(start, end, page, size);
        }

        log.info("No filter applied, returning all customers");
        return customerService.getAllCustomers(page, size);
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id The UUID of the customer to delete.
     * @return A {@link Mono} signaling when the deletion is complete.
     */
    @DeleteMapping("/{id}")
    public Mono<Void> deleteCustomer(@PathVariable UUID id) {
        log.warn("Request to delete customer with ID: {}", id);
        return customerService.deleteCustomer(id);
    }
}