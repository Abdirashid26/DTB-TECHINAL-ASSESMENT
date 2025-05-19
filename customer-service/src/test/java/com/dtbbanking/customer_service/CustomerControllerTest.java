package com.dtbbanking.customer_service;

import com.dtbbanking.customer_service.controller.CustomerController;
import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import com.dtbbanking.customer_service.dto.UniversalResponse;
import com.dtbbanking.customer_service.dto.UpdateCustomerRequestDto;
import com.dtbbanking.customer_service.service.CustomerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private Validator validator;

    @InjectMocks
    private CustomerController customerController;

    private UUID customerId;
    private CustomerRequestDto validRequestDto;
    private CustomerResponseDto responseDto;
    private UniversalResponse<CustomerResponseDto> universalResponse;
    private UpdateCustomerRequestDto updateRequestDto;
    private LocalDateTime now;
    private List<CustomerResponseDto> responseList;
    private UniversalResponse<List<CustomerResponseDto>> universalResponseList;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        now = LocalDateTime.now();

        validRequestDto = new CustomerRequestDto();
        validRequestDto.setFirstName("John");
        validRequestDto.setLastName("Doe");
        validRequestDto.setEmail("john.doe@example.com");
        validRequestDto.setPhoneNumber("1234567890");
        validRequestDto.setNationalId("ID12345");
        validRequestDto.setDateOfBirth(LocalDateTime.of(1990, 1, 1, 0, 0));

        responseDto = CustomerResponseDto.builder()
                .id(customerId)
                .fullName("John Doe")
                .createdAt(now)
                .build();

        universalResponse = UniversalResponse.ok(responseDto);
        responseList = List.of(responseDto);
        universalResponseList = UniversalResponse.ok(responseList);

        updateRequestDto = new UpdateCustomerRequestDto();
        updateRequestDto.setFirstName("Jane");
        updateRequestDto.setLastName("Smith");
    }

    @Test
    void createCustomer_WithValidData_ShouldReturnOk() {
        when(validator.validate(validRequestDto)).thenReturn(Collections.emptySet());
        when(customerService.createCustomer(validRequestDto)).thenReturn(Mono.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<?>>> result = customerController.createCustomer(validRequestDto);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponse))
                .verifyComplete();

        verify(customerService).createCustomer(validRequestDto);
    }

    @Test
    void updateCustomer_ShouldReturnUpdatedCustomer() {
        when(customerService.updateCustomer(eq(customerId), any(UpdateCustomerRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<CustomerResponseDto>>> result =
                customerController.updateCustomer(customerId, updateRequestDto);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponse))
                .verifyComplete();

        verify(customerService).updateCustomer(customerId, updateRequestDto);
    }

    @Test
    void getCustomer_ShouldReturnCustomer() {
        when(customerService.getCustomerById(customerId)).thenReturn(Mono.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<CustomerResponseDto>>> result = customerController.getCustomer(customerId);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponse))
                .verifyComplete();

        verify(customerService).getCustomerById(customerId);
    }

    @Test
    void getCustomers_WithNoFilters_ShouldReturnAllCustomers() {
        when(customerService.getAllCustomers(0, 10)).thenReturn(Flux.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<List<CustomerResponseDto>>>> result =
                customerController.getCustomers(null, null, null, 0, 10);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponseList))
                .verifyComplete();

        verify(customerService).getAllCustomers(0, 10);
    }

    @Test
    void getCustomers_WithNameFilter_ShouldReturnFilteredCustomers() {
        String name = "John";
        when(customerService.getCustomersByFirstName(name, 0, 10)).thenReturn(Flux.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<List<CustomerResponseDto>>>> result =
                customerController.getCustomers(name, null, null, 0, 10);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponseList))
                .verifyComplete();

        verify(customerService).getCustomersByFirstName(name, 0, 10);
    }

    @Test
    void getCustomers_WithDateFilter_ShouldReturnFilteredCustomers() {
        LocalDateTime start = now.minusDays(1);
        LocalDateTime end = now.plusDays(1);
        when(customerService.getCustomersByCreatedDate(start, end, 0, 10))
                .thenReturn(Flux.just(responseDto));

        Mono<ResponseEntity<UniversalResponse<List<CustomerResponseDto>>>> result =
                customerController.getCustomers(null, start, end, 0, 10);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(universalResponseList))
                .verifyComplete();

        verify(customerService).getCustomersByCreatedDate(start, end, 0, 10);
    }

    @Test
    void deleteCustomer_ShouldReturnOkResponse() {
        when(customerService.deleteCustomer(customerId)).thenReturn(Mono.empty());

        Mono<ResponseEntity<UniversalResponse<Void>>> result = customerController.deleteCustomer(customerId);

        StepVerifier.create(result)
                .expectNext(ResponseEntity.ok(UniversalResponse.ok(null)))
                .verifyComplete();

        verify(customerService).deleteCustomer(customerId);
    }
}