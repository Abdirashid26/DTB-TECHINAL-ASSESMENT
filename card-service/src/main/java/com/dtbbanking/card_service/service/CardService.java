package com.dtbbanking.card_service.service;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.errors.CustomerNotFoundException;
import com.dtbbanking.card_service.mapper.CardMapper;
import com.dtbbanking.card_service.model.Card;
import com.dtbbanking.card_service.model.CardType;
import com.dtbbanking.card_service.repository.CardRepository;
import com.dtbbanking.card_service.utils.CardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service class for card management, including creation, retrieval,
 * updating alias, deletion, and filtering.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final WebClient accountWebClient;

    /**
     * Checks if an account with the given ID exists by calling the Account service.
     *
     * @param accountId UUID of the account.
     * @return Mono emitting true if the account exists, otherwise an error.
     */
    private Mono<Boolean> checkAccountExists(UUID accountId) {
        return accountWebClient.get()
                .uri("/{accountId}", accountId)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        response -> {
                            log.warn("Account not found: {}", accountId);
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account does not exist"));
                        }
                )
                .toBodilessEntity()
                .thenReturn(true);
    }

    /**
     * Creates a new card for an account, enforcing business rules such as
     * maximum cards per account and card type uniqueness.
     *
     * @param dto The card request DTO containing card details.
     * @return Mono emitting the created card response DTO.
     */
    public Mono<CardResponseDto> createCard(CardRequestDto dto) {
        return checkAccountExists(dto.getAccountId())
                .flatMap(exists -> cardRepository.countByAccountId(dto.getAccountId()))
                .flatMap(count -> {
                    if (count >= 2) {
                        return Mono.error(new CustomerNotFoundException( "Account already has 2 cards"));
                    }
                    return cardRepository.existsByAccountIdAndCardType(dto.getAccountId(), dto.getCardType());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new CustomerNotFoundException("Account already has a card of this type"));
                    }
                    Card card = cardMapper.toEntity(dto);
                    card.setPan(CardUtils.generatePan());
                    card.setCvv(CardUtils.generateCvv());
                    card.setCreatedAt(LocalDateTime.now());
                    card.setUpdatedAt(LocalDateTime.now());
                    return cardRepository.save(card);
                })
                .map(cardMapper::toDto)
                .map(this::maskSensitiveData);
    }

    /**
     * Retrieves a card by its ID with optional masking of sensitive data.
     *
     * @param id     The card UUID.
     * @param unmask Whether to return sensitive fields unmasked.
     * @return Mono emitting the card response DTO.
     */
    public Mono<CardResponseDto> getCardById(UUID id, boolean unmask) {
        return cardRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")))
                .map(cardMapper::toDto)
                .zipWith(cardRepository.findById(id))
                .map(tuple2 -> unmask ? cardMapper.toDtoUnmasked(tuple2.getT2()) : maskSensitiveData(tuple2.getT1()));
    }

    /**
     * Retrieves cards filtered by alias, type, or PAN with pagination and masking options.
     *
     * @param alias  Optional card alias filter.
     * @param type   Optional card type filter.
     * @param pan    Optional PAN filter.
     * @param page   Page number for pagination.
     * @param size   Page size for pagination.
     * @param unmask Whether to unmask sensitive data.
     * @return Flux stream of filtered card response DTOs.
     */
    public Flux<CardResponseDto> getCardsByFilters(String alias, CardType type, String pan, int page, int size, boolean unmask) {
        Pageable pageable = PageRequest.of(page, size);

        Flux<Card> cards;

        if (alias != null) {
            cards = cardRepository.findByCardAliasContainingIgnoreCase(alias, pageable);
        } else if (type != null) {
            cards = cardRepository.findByCardType(type, pageable);
        } else if (pan != null) {
            cards = cardRepository.findByPanContaining(pan, pageable);
        } else {
            cards = cardRepository.findAll().skip((long) page * size).take(size);
        }

        return cards
                .map(card -> {
                    CardResponseDto dto = unmask ? cardMapper.toDtoUnmasked(card) : cardMapper.toDto(card);
                    return unmask ? dto : maskSensitiveData(dto);
                });
    }

    /**
     * Updates the alias of a card if it doesn't conflict with existing aliases under the same account.
     *
     * @param cardId   UUID of the card to update.
     * @param newAlias The new alias to set.
     * @return Mono emitting the updated card response DTO.
     */
    public Mono<CardResponseDto> updateCardAlias(UUID cardId, String newAlias) {
        return cardRepository.findById(cardId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")))
                .flatMap(card ->
                        cardRepository.existsByAccountIdAndCardAliasIgnoreCaseAndIdIsNot(card.getAccountId(), newAlias, cardId)
                                .flatMap(duplicateExists -> {
                                    if (duplicateExists) {
                                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate card alias for this account"));
                                    }
                                    card.setCardAlias(newAlias);
                                    card.setUpdatedAt(LocalDateTime.now());
                                    return cardRepository.save(card);
                                })
                )
                .map(cardMapper::toDto)
                .map(this::maskSensitiveData);
    }

    /**
     * Deletes a card by its ID.
     *
     * @param id UUID of the card to delete.
     * @return Mono signaling completion.
     */
    public Mono<Void> deleteCard(UUID id) {
        return cardRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")))
                .flatMap(card -> cardRepository.deleteById(card.getId()));
    }

    /**
     * Masks the PAN and CVV in the CardResponseDto.
     *
     * @param dto The card response DTO to mask.
     * @return The masked card response DTO.
     */
    private CardResponseDto maskSensitiveData(CardResponseDto dto) {
        dto.setPan(CardUtils.maskPan(dto.getPan()));
        dto.setCvv(CardUtils.maskCvv(dto.getCvv()));
        return dto;
    }

    /**
     * Retrieves account IDs by card alias (internal endpoint).
     *
     * @param alias The card alias filter.
     * @return Flux stream of account UUIDs matching the alias.
     */
    @GetMapping("/internal/cards/account-ids")
    public Flux<UUID> getAccountIdsByCardAlias(@RequestParam String alias) {
        return cardRepository.findByCardAliasContainingIgnoreCase(alias)
                .map(Card::getAccountId);
    }
}