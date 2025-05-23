package com.dtbbanking.card_service;

import com.dtbbanking.card_service.controller.CardController;
import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.dto.UpdateCardAliasRequest;
import com.dtbbanking.card_service.model.CardType;
import com.dtbbanking.card_service.service.CardService;

import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardControllerTest {

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    @Mock
    private Validator validator;

    private WebTestClient webTestClient;

    private UUID cardId;
    private UUID accountId;
    private CardRequestDto cardRequestDto;
    private CardResponseDto cardResponseDto;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(cardController).build();

        cardId = UUID.randomUUID();
        accountId = UUID.randomUUID();

        cardRequestDto = new CardRequestDto();
        cardRequestDto.setCardAlias("MyCard");
        cardRequestDto.setAccountId(accountId);
        cardRequestDto.setCardType(CardType.VIRTUAL);

        cardResponseDto = CardResponseDto.builder()
                .id(cardId)
                .cardAlias("MyCard")
                .accountId(accountId)
                .cardType(CardType.VIRTUAL)
                .pan("****-****-****-1234")
                .cvv("123")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createCard_shouldReturnCreatedCard() {
        when(validator.validate(any(CardRequestDto.class))).thenReturn(Collections.emptySet());
        when(cardService.createCard(cardRequestDto)).thenReturn(Mono.just(cardResponseDto));

        webTestClient.post()
                .uri("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(cardRequestDto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.data.cardAlias").isEqualTo("MyCard");
    }

    @Test
    void getCardById_shouldReturnCard() {
        when(cardService.getCardById(cardId, false)).thenReturn(Mono.just(cardResponseDto));

        webTestClient.get()
                .uri("/api/v1/cards/{id}", cardId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.cardAlias").isEqualTo("MyCard");
    }

    @Test
    void getCardsByFilters_shouldReturnList() {
        when(cardService.getCardsByFilters(null, null, null, 0, 10, false))
                .thenReturn(Flux.just(cardResponseDto));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/cards")
                        .queryParam("page", "0")
                        .queryParam("size", "10")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data[0].cardAlias").isEqualTo("MyCard");
    }

    @Test
    void updateCardAlias_shouldReturnUpdatedCard() {
        String newAlias = "UpdatedAlias";
        UpdateCardAliasRequest updateRequest = new UpdateCardAliasRequest();
        updateRequest.setNewAlias(newAlias);

        CardResponseDto updatedCard = CardResponseDto.builder()
                .id(cardId)
                .cardAlias(newAlias)
                .accountId(accountId)
                .cardType(CardType.VIRTUAL)
                .pan("****-****-****-1234")
                .cvv("123")
                .createdAt(LocalDateTime.now())
                .build();

        when(cardService.updateCardAlias(cardId, newAlias)).thenReturn(Mono.just(updatedCard));

        webTestClient.patch()
                .uri("/api/v1/cards/alias/{id}", cardId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.cardAlias").isEqualTo("UpdatedAlias");
    }

    @Test
    void deleteCard_shouldReturnNoContent() {
        when(cardService.deleteCard(cardId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/cards/{id}", cardId)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getAccountIdsByCardAlias_shouldReturnListOfAccountIds() {
        String alias = "MyCard";
        UUID acc1 = UUID.randomUUID();
        UUID acc2 = UUID.randomUUID();

        when(cardService.getAccountIdsByCardAlias(alias)).thenReturn(Flux.just(acc1, acc2));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/cards/internal/cards/account-ids")
                        .queryParam("alias", alias)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(2)
                .jsonPath("$.data[0]").isEqualTo(acc1.toString())
                .jsonPath("$.data[1]").isEqualTo(acc2.toString());
    }
}
