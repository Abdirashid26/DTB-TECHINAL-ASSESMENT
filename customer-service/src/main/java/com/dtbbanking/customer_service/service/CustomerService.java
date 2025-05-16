package com.dtbbanking.customer_service.service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Mono<CustomerResponseDto> createCustomer(CustomerRequestDto dto) {
        Customer customer = CustomerMapper.toEntity(dto);
        log.info("Creating new customer: {}", customer.getEmail());

        // If phoneNumber or nationalId is not provided, skip duplicate check
        if (dto.getPhoneNumber() == null || dto.getNationalId() == null) {
            return customerRepository.save(customer)
                    .doOnSuccess(saved -> log.info("Customer created with ID: {}", saved.getId()))
                    .map(CustomerMapper::toResponseDto);
        }

        return customerRepository.existsByPhoneNumberOrNationalId(dto.getPhoneNumber(), dto.getNationalId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateResourceException("Phone number or National ID already exists"));
                    }
                    return customerRepository.save(customer)
                            .doOnSuccess(saved -> log.info("Customer created with ID: {}", saved.getId()))
                            .map(CustomerMapper::toResponseDto);
                });
    }

    public Mono<CustomerResponseDto> getCustomerById(UUID id) {
        log.debug("Fetching customer by ID: {}", id);

        return customerRepository.findById(id)
                .doOnNext(c -> log.info("Found customer: {} {}", c.getFirstName(), c.getLastName()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found")))
                .map(CustomerMapper::toResponseDto);
    }

    public Flux<CustomerResponseDto> getCustomersByFirstName(String name, int page, int size) {
        log.debug("Searching customers by name like: {}", name);

        return customerRepository.searchByFullName(name, PageRequest.of(page, size))
                .map(CustomerMapper::toResponseDto);
    }

    public Flux<CustomerResponseDto> getCustomersByCreatedDate(LocalDateTime start, LocalDateTime end, int page, int size) {
        log.debug("Searching customers created between {} and {}", start, end);

        return customerRepository.findByCreatedAtBetween(start, end, PageRequest.of(page, size))
                .map(CustomerMapper::toResponseDto);
    }

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


    public Flux<CustomerResponseDto> getAllCustomers(int page, int size) {
        return customerRepository.findAll()
                .skip((long) page * size)
                .take(size)
                .map(CustomerMapper::toResponseDto);
    }




}