package com.dtbbanking.customer_service.service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.dto.UpdateCustomerRequestDto;
import com.dtbbanking.customer_service.errors.DuplicateResourceException;
import com.dtbbanking.customer_service.mapper.CustomerMapper;
import com.dtbbanking.customer_service.models.Customer;
import com.dtbbanking.customer_service.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class responsible for customer-related business logic.
 *
 * Handles creation, update, retrieval, filtering, and deletion of customer records.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    /**
     * Creates a new customer in the system.
     *
     * @param dto The request DTO containing the customer data.
     * @return A {@link Mono} emitting the created customer's response DTO.
     */
    public Mono<CustomerResponseDto> createCustomer(CustomerRequestDto dto) {
        Customer customer = CustomerMapper.toEntity(dto);
        log.info("Creating new customer: {}", customer.getFirstName());

        return customerRepository.save(customer)
                .doOnSuccess(saved -> log.info("Customer created with ID: {}", saved.getId()))
                .map(CustomerMapper::toResponseDto);
    }

    /**
     * Updates an existing customer.
     *
     * @param id  The UUID of the customer to update.
     * @param dto The DTO containing fields to update.
     * @return A {@link Mono} emitting the updated customer's response DTO.
     */
    public Mono<CustomerResponseDto> updateCustomer(UUID id, UpdateCustomerRequestDto dto) {
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
                .flatMap(existingCustomer -> {
                    if (dto.getFirstName() != null) {
                        existingCustomer.setFirstName(dto.getFirstName());
                    }
                    if (dto.getLastName() != null) {
                        existingCustomer.setLastName(dto.getLastName());
                    }
                    if (dto.getOtherName() != null) {
                        existingCustomer.setOtherName(dto.getOtherName());
                    }
                    return customerRepository.save(existingCustomer);
                })
                .doOnSuccess(updated -> log.info("Customer updated with ID: {}", updated.getId()))
                .map(CustomerMapper::toResponseDto);
    }

    /**
     * Retrieves a customer by their unique ID.
     *
     * @param id The UUID of the customer.
     * @return A {@link Mono} emitting the found customer's response DTO, or an error if not found.
     */
    public Mono<CustomerResponseDto> getCustomerById(UUID id) {
        log.debug("Fetching customer by ID: {}", id);

        return customerRepository.findById(id)
                .doOnNext(c -> log.info("Found customer: {} {}", c.getFirstName(), c.getLastName()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
                .map(CustomerMapper::toResponseDto);
    }

    /**
     * Retrieves a paginated list of customers filtered by first name.
     *
     * @param name The name to filter customers by.
     * @param page The page number (zero-based).
     * @param size The page size.
     * @return A {@link Flux} emitting matching {@link CustomerResponseDto} objects.
     */
    public Flux<CustomerResponseDto> getCustomersByFirstName(String name, int page, int size) {
        log.debug("Searching customers by name like: {}", name);

        return customerRepository.searchByFullName(name, PageRequest.of(page, size))
                .map(CustomerMapper::toResponseDto);
    }

    /**
     * Retrieves a paginated list of customers created within a specific date range.
     *
     * @param start Start of the date range.
     * @param end   End of the date range.
     * @param page  The page number (zero-based).
     * @param size  The page size.
     * @return A {@link Flux} emitting matching {@link CustomerResponseDto} objects.
     */
    public Flux<CustomerResponseDto> getCustomersByCreatedDate(LocalDateTime start, LocalDateTime end, int page, int size) {
        log.debug("Searching customers created between {} and {}", start, end);

        return customerRepository.findByCreatedAtBetween(start, end, PageRequest.of(page, size))
                .map(CustomerMapper::toResponseDto);
    }

    /**
     * Deletes a customer by their ID.
     *
     * @param id The UUID of the customer to delete.
     * @return A {@link Mono} that completes when the customer is deleted or errors if not found.
     */
    public Mono<Void> deleteCustomer(UUID id) {
        return customerRepository.existsById(id)
                .flatMap(exists -> {
                    if (exists) {
                        return customerRepository.deleteById(id);
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
                    }
                });
    }

    /**
     * Retrieves all customers with pagination.
     *
     * @param page The page number (zero-based).
     * @param size The page size.
     * @return A {@link Flux} emitting all customers within the given page.
     */
    public Flux<CustomerResponseDto> getAllCustomers(int page, int size) {
        return customerRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(CustomerMapper::toResponseDto);
    }
}