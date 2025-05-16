package com.dtbbanking.customer_service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.errors.DuplicateResourceException;
import com.dtbbanking.customer_service.models.Customer;
import com.dtbbanking.customer_service.repository.CustomerRepository;
import com.dtbbanking.customer_service.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;


public class CustomerServiceTest {

    private CustomerRepository customerRepository;
    private CustomerService customerService;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        customerRepository = Mockito.mock(CustomerRepository.class);
        customerService = new CustomerService(customerRepository);

        savedCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .firstName("Faisal")
                .lastName("Abdirashid")
                .email("faisaldev26@gmail.com")
                .phoneNumber("254795881812")
                .nationalId("38481165")
                .dateOfBirth(LocalDateTime.of(2024, 1, 1, 0, 0))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testCreateCustomer_success() {
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setFirstName("Faisal");
        dto.setLastName("Abdirashid");
        dto.setEmail("faisaldev26@gmail.com");
        dto.setPhoneNumber("254795881812");
        dto.setNationalId("38481165");
        dto.setDateOfBirth(LocalDateTime.of(2024, 1, 1, 0, 0));

        when(customerRepository.existsByPhoneNumberOrNationalId(dto.getPhoneNumber(), dto.getNationalId()))
                .thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class))).thenReturn(Mono.just(savedCustomer));

        StepVerifier.create(customerService.createCustomer(dto))
                .expectNextMatches(response -> response.getEmail().equals("faisaldev26@gmail.com"))
                .verifyComplete();
    }

    @Test
    void testCreateCustomer_duplicatePhoneOrId() {
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setPhoneNumber("254795881812");
        dto.setNationalId("38481165");

        when(customerRepository.existsByPhoneNumberOrNationalId(dto.getPhoneNumber(), dto.getNationalId()))
                .thenReturn(Mono.just(true));

        StepVerifier.create(customerService.createCustomer(dto))
                .expectError(DuplicateResourceException.class)
                .verify();
    }

    @Test
    void testGetCustomerById_success() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Mono.just(savedCustomer));

        StepVerifier.create(customerService.getCustomerById(UUID.randomUUID()))
                .expectNextMatches(response -> response.getFullName().equals("Faisal Abdirashid"))
                .verifyComplete();
    }

    @Test
    void testGetCustomerById_notFound() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.getCustomerById(UUID.randomUUID()))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().is4xxClientError()
                )
                .verify();
    }

    @Test
    void testGetCustomersByFirstName() {
        when(customerRepository.searchByFullName(eq("Faisal"), any()))
                .thenReturn(Flux.just(savedCustomer));

        StepVerifier.create(customerService.getCustomersByFirstName("Faisal", 0, 10))
                .expectNextMatches(response -> response.getFullName().startsWith("Faisal"))
                .verifyComplete();
    }

    @Test
    void testGetCustomersByCreatedDate() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        when(customerRepository.findByCreatedAtBetween(eq(start), eq(end), any(PageRequest.class)))
                .thenReturn(Flux.just(savedCustomer));

        StepVerifier.create(customerService.getCustomersByCreatedDate(start, end, 0, 10))
                .expectNextMatches(response -> response.getEmail().equals("faisaldev26@gmail.com"))
                .verifyComplete();
    }

    @Test
    void testDeleteCustomer_success() {
        UUID id = UUID.randomUUID();

        when(customerRepository.existsById(id)).thenReturn(Mono.just(true));
        when(customerRepository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer(id))
                .verifyComplete();
    }

    @Test
    void testDeleteCustomer_notFound() {
        when(customerRepository.existsById(any(UUID.class))).thenReturn(Mono.just(false));

        StepVerifier.create(customerService.deleteCustomer(UUID.randomUUID()))
                .expectErrorMatches(throwable ->
                        throwable instanceof ResponseStatusException &&
                                ((ResponseStatusException) throwable).getStatusCode().is4xxClientError()
                )
                .verify();
    }

    @Test
    void testGetAllCustomers_success() {
        when(customerRepository.findAll())
                .thenReturn(Flux.just(savedCustomer));

        StepVerifier.create(customerService.getAllCustomers(0, 10))
                .expectNextMatches(c -> c.getEmail().equals("faisaldev26@gmail.com"))
                .verifyComplete();
    }

    @Test
    void testGetAllCustomers_empty() {
        when(customerRepository.findAll())
                .thenReturn(Flux.empty());

        StepVerifier.create(customerService.getAllCustomers(0, 10))
                .expectNextCount(0)
                .verifyComplete();
    }
}
