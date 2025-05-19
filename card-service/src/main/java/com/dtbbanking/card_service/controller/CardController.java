package com.dtbbanking.card_service.controller;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.dto.UniversalResponse;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for managing cards.
 * Supports operations like create, retrieve, filter, update alias, and delete.
 */
@RestController
@RequestMapping("/api/v1/cards")
public class CardController {

    private final CardService cardService;
    private final Validator validator;

    @Autowired
    public CardController(CardService cardService, Validator validator) {
        this.cardService = cardService;
        this.validator = validator;
    }

    /**
     * Creates a new card linked to an account.
     *
     * @param dto The card creation request containing alias, account ID, and type.
     * @return A response containing the created card or validation errors.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<UniversalResponse<?>>> createCard(@RequestBody CardRequestDto dto) {
        Set<ConstraintViolation<CardRequestDto>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            Map<String, String> errors = violations.stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage
                    ));
            return Mono.just(ResponseEntity.badRequest()
                    .body(UniversalResponse.error(400, "Validation failed", errors)));
        }

        return cardService.createCard(dto)
                .map(card -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(UniversalResponse.ok(card)));
    }

    /**
     * Retrieves a list of account IDs associated with the given card alias.
     * This endpoint is intended for internal use.
     *
     * @param alias The card alias used to filter cards.
     * @return A response containing a list of account UUIDs.
     */
    @GetMapping("/internal/cards/account-ids")
    public Mono<ResponseEntity<UniversalResponse<List<UUID>>>> getAccountIdsByCardAlias(@RequestParam String alias) {
        return cardService.getAccountIdsByCardAlias(alias)
                .collectList()
                .map(accountIds -> ResponseEntity.ok(UniversalResponse.ok(accountIds)));
    }


    /**
     * Retrieves a card by its ID.
     *
     * @param id     The UUID of the card to fetch.
     * @param unmask Whether to return the full PAN and CVV values (true) or masked versions (false).
     * @return A response containing the card details.
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<UniversalResponse<CardResponseDto>>> getCardById(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean unmask) {
        return cardService.getCardById(id, unmask)
                .map(card -> ResponseEntity.ok(UniversalResponse.ok(card)));
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
     * @return A response containing a list of matching card records.
     */
    @GetMapping
    public Mono<ResponseEntity<UniversalResponse<List<CardResponseDto>>>> getCardsByFilters(
            @RequestParam(required = false) String alias,
            @RequestParam(required = false) CardType type,
            @RequestParam(required = false) String pan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "false") boolean unmask) {

        return cardService.getCardsByFilters(alias, type, pan, page, size, unmask)
                .collectList()
                .map(cards -> ResponseEntity.ok(UniversalResponse.ok(cards)));
    }

    /**
     * Updates the alias of a specific card.
     *
     * @param id      The UUID of the card to update.
     * @param request The request containing the new alias.
     * @return A response containing the updated card details.
     */
    @PatchMapping("alias/{id}")
    public Mono<ResponseEntity<UniversalResponse<CardResponseDto>>> updateCardAlias(
            @PathVariable UUID id,
            @RequestBody UpdateCardAliasRequest request) {
        return cardService.updateCardAlias(id, request.getNewAlias())
                .map(updated -> ResponseEntity.ok(UniversalResponse.ok(updated)));
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
