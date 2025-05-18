package com.dtbbanking.account_service.service;

import com.dtbbanking.account_service.dto.CustomerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomerClient {


    private final WebClient customerServiceWebClient;

    public Mono<Boolean> existsById(UUID customerId) {
        return customerServiceWebClient.get()
                .uri("/{id}", customerId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                        return Mono.empty();
                    }
                    return clientResponse.createException();
                })
                .bodyToMono(CustomerResponseDto.class)
                .map(Objects::nonNull)
                .defaultIfEmpty(false);
    }

}