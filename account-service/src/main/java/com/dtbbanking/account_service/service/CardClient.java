package com.dtbbanking.account_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CardClient {

    private final WebClient cardServiceWebClient;

    public Mono<List<UUID>> getAccountIdsByCardAlias(String alias) {
        return cardServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/cards/account-ids")
                        .queryParam("alias", alias)
                        .build())
                .retrieve()
                .bodyToFlux(UUID.class)
                .collectList();
    }
}
