package com.dtbbanking.card_service.repository;

import com.dtbbanking.card_service.model.Card;
import com.dtbbanking.card_service.model.CardType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CardRepository extends R2dbcRepository<Card, UUID> {

    // Filter by alias (supports pagination)
    Flux<Card> findByCardAliasContainingIgnoreCase(String cardAlias, Pageable pageable);

    // Filter by alias
    Flux<Card> findByCardAliasContainingIgnoreCase(String cardAlias);

    // Filter by card type
    Flux<Card> findByCardType(CardType cardType, Pageable pageable);

    // Filter by partial PAN
    Flux<Card> findByPanContaining(String pan, Pageable pageable);

    // Ensure each account has max 1 of each type
    Mono<Boolean> existsByAccountIdAndCardType(UUID accountId, CardType cardType);

    // Ensure max 2 cards per account
    Mono<Long> countByAccountId(UUID accountId);

    // Check if alias is already taken by another card under same account
    Mono<Boolean> existsByAccountIdAndCardAliasIgnoreCaseAndIdIsNot(UUID accountId, String cardAlias, UUID excludedId);

}