package com.dtbbanking.card_service.controller;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.dto.UpdateCardAliasRequest;
import com.dtbbanking.card_service.model.CardType;
import com.dtbbanking.card_service.service.CardService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing cards.
 */
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;
    private final Validator validator;

    @Autowired
    public CardController(CardService cardService,Validator validator) {
        this.cardService = cardService;
        this.validator = validator;
    }

    /**
     * Creates a new card linked to an account.
     *
     * @param dto The card creation request containing alias, account ID, and type.
     * @return The created card's details.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<?>> createCard(@RequestBody CardRequestDto dto) {
        Set<ConstraintViolation<CardRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            return Mono.just(ResponseEntity.badRequest().body(errors));
        }
        return cardService.createCard(dto)
                .map(cardResponseDto -> ResponseEntity.status(HttpStatus.CREATED).body(cardResponseDto));
    }


    /**
     * Retrieves a list of account IDs associated with the given card alias.
     * <p>
     * This endpoint is intended for internal use to fetch all account UUIDs
     * that have cards with the specified alias.
     * </p>
     *
     * @param alias The card alias used to filter cards.
     * @return A reactive Mono emitting a List of UUIDs representing account IDs
     * that own cards matching the alias.
     */
    @GetMapping("/internal/cards/account-ids")
    public Flux<UUID> getAccountIdsByCardAlias(@RequestParam String alias) {
        return cardService.getAccountIdsByCardAlias(alias);
    }

    /**
     * Retrieves a card by its ID.
     *
     * @param id     The UUID of the card to fetch.
     * @param unmask Whether to return the full PAN and CVV values (true) or masked versions (false).
     * @return The card's details, masked or unmasked.
     */
    @GetMapping("/{id}")
    public Mono<CardResponseDto> getCardById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean unmask) {
        return cardService.getCardById(id, unmask);
    }

    /**
     * Retrieves a list of cards filtered by optional parameters.
     *
     * @param alias   Optional card alias to filter.
     * @param type    Optional card type (VIRTUAL or PHYSICAL) to filter.
     * @param pan     Optional PAN to filter (exact match).
     * @param page    Page number for pagination (default is 0).
     * @param size    Page size for pagination (default is 10).
     * @param unmask  Whether to return full PAN/CVV or masked values.
     * @return A reactive stream of matching card records.
     */
    @GetMapping
    public Flux<CardResponseDto> getCardsByFilters(
            @RequestParam(required = false) String alias,
            @RequestParam(required = false) CardType type,
            @RequestParam(required = false) String pan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unmask) {
        return cardService.getCardsByFilters(alias, type, pan, page, size, unmask);
    }

    /**
     * Updates the alias of a specific card.
     *
     * @param id      The UUID of the card to update.
     * @param request The request containing the new alias.
     * @return The updated card's details.
     */
    @PatchMapping("alias/{id}")
    public Mono<CardResponseDto> updateCardAlias(
            @PathVariable UUID id,
            @RequestBody UpdateCardAliasRequest request) {
        return cardService.updateCardAlias(id, request.getNewAlias());
    }

    /**
     * Deletes a card by its ID.
     *
     * @param id The UUID of the card to delete.
     * @return An empty response indicating successful deletion.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCard(@PathVariable UUID id) {
        return cardService.deleteCard(id);
    }
}
