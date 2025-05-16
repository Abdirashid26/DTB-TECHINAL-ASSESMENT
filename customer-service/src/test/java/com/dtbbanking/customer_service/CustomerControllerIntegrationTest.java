package com.dtbbanking.customer_service;

import com.dtbbanking.customer_service.dto.CustomerRequestDto;
import com.dtbbanking.customer_service.dto.CustomerResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomerControllerIntegrationTest {

    @Container
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("dtbbanking")
            .withUsername("dtb")
            .withPassword("dtbpassword")
            .withInitScript("schema.sql");

    @Autowired
    private WebTestClient webTestClient;

    private static UUID savedCustomerId;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        postgres.getHost(),
                        postgres.getFirstMappedPort(),
                        postgres.getDatabaseName()));
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);
    }

    @Test
    @Order(1)
    void shouldCreateCustomerSuccessfully() {
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setFirstName("Faisal");
        dto.setLastName("Abdirashid");
        dto.setEmail("faisaldev26@gmail.com");
        dto.setPhoneNumber("254795881812");
        dto.setNationalId("38481165");
        dto.setDateOfBirth(LocalDateTime.of(2024, 1, 1, 0, 0));

        CustomerResponseDto response = webTestClient.post()
                .uri("/api/v1/customers")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponseDto.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(dto.getEmail());
        savedCustomerId = response.getId();
    }

    @Test
    @Order(2)
    void shouldRejectDuplicateCustomer() {
        CustomerRequestDto dto = new CustomerRequestDto();
        dto.setFirstName("Faisal");
        dto.setLastName("Abdirashid");
        dto.setEmail("another@gmail.com");
        dto.setPhoneNumber("254795881812"); // same as previous
        dto.setNationalId("38481165"); // same as previous
        dto.setDateOfBirth(LocalDateTime.of(2024, 1, 1, 0, 0));

        webTestClient.post()
                .uri("/api/v1/customers")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.error").isEqualTo("Conflict")
                .jsonPath("$.message").exists();
    }

    @Test
    @Order(3)
    void shouldValidateMissingFields() {
        CustomerRequestDto dto = new CustomerRequestDto(); // empty DTO

        webTestClient.post()
                .uri("/api/v1/customers")
                .bodyValue(dto)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(Map.class)
                .value(errors ->
                        assertThat(errors).containsKeys("firstName", "lastName")
                );    }

    @Test
    @Order(4)
    void shouldReturnCustomerById() {
        webTestClient.get()
                .uri("/api/v1/customers/{id}", savedCustomerId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(savedCustomerId.toString());
    }

    @Test
    @Order(5)
    void shouldReturnAllCustomers() {
        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponseDto.class)
                .value(list -> assertThat(list).isNotEmpty());
    }

    @Test
    @Order(6)
    void shouldDeleteCustomerAndNotFindItAfter() {
        webTestClient.delete()
                .uri("/api/v1/customers/{id}", savedCustomerId)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/v1/customers/{id}", savedCustomerId)
                .exchange()
                .expectStatus().isNotFound();
    }
}

