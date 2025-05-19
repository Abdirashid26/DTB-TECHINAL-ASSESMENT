package com.dtbbanking.customer_service.controller;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.dto.UniversalResponse;
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
import java.util.List;
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
     * @return A {@link Mono} emitting a UniversalResponse with the created customer or validation errors.
     */
    @PostMapping
    public Mono<ResponseEntity<UniversalResponse<?>>> createCustomer(@RequestBody CustomerRequestDto dto) {
        Set<ConstraintViolation<CustomerRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            return Mono.just(ResponseEntity
                    .badRequest()
                    .body(UniversalResponse.error(400, "Validation failed", errors)));
        }
        return customerService.createCustomer(dto)
                .map(result -> ResponseEntity.ok(UniversalResponse.ok(result)));
    }

    /**
     * Updates an existing customer.
     *
     * @param id  The UUID of the customer to update.
     * @param dto The DTO containing updated customer information.
     * @return A {@link Mono} emitting a UniversalResponse with the updated customer.
     */
    @PutMapping("/{id}")
    public Mono<ResponseEntity<UniversalResponse<CustomerResponseDto>>> updateCustomer(
            @PathVariable UUID id,
            @RequestBody UpdateCustomerRequestDto dto) {

        return customerService.updateCustomer(id, dto)
                .map(result -> ResponseEntity.ok(UniversalResponse.ok(result)));
    }

    /**
     * Retrieves a customer by ID.
     *
     * @param id The UUID of the customer.
     * @return A {@link Mono} emitting a UniversalResponse with the customer data.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UniversalResponse<CustomerResponseDto>>> getCustomer(@PathVariable UUID id) {
        log.info("Received request to fetch customer with ID: {}", id);
        return customerService.getCustomerById(id)
                .map(result -> ResponseEntity.ok(UniversalResponse.ok(result)));
    }

    /**
     * Retrieves customers with optional filtering.
     *
     * @param name  Optional name filter.
     * @param start Optional start datetime for creation date filtering.
     * @param end   Optional end datetime for creation date filtering.
     * @param page  Page number for pagination (default is 0).
     * @param size  Page size for pagination (default is 10).
     * @return A {@link Mono} emitting a UniversalResponse with a list of customers.
     */
    @GetMapping
    public Mono<ResponseEntity<UniversalResponse<List<CustomerResponseDto>>>> getCustomers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Mono<List<CustomerResponseDto>> resultMono;

        if (name != null) {
            log.info("Filtering customers by name='{}', page={}, size={}", name, page, size);
            resultMono = customerService.getCustomersByFirstName(name, page, size).collectList();
        } else if (start != null && end != null) {
            log.info("Filtering customers by creation date between {} and {}", start, end);
            resultMono = customerService.getCustomersByCreatedDate(start, end, page, size).collectList();
        } else {
            log.info("No filter applied, returning all customers");
            resultMono = customerService.getAllCustomers(page, size).collectList();
        }

        return resultMono.map(list -> ResponseEntity.ok(UniversalResponse.ok(list)));
    }

    /**
     * Deletes a customer by ID.
     *
     * @param id The UUID of the customer to delete.
     * @return A {@link Mono} emitting a UniversalResponse indicating deletion success.
     */
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<UniversalResponse<Void>>> deleteCustomer(@PathVariable UUID id) {
        log.warn("Request to delete customer with ID: {}", id);
        return customerService.deleteCustomer(id)
                .thenReturn(ResponseEntity.ok(UniversalResponse.ok(null)));
    }
}
