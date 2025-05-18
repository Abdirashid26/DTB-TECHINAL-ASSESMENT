package com.dtbbanking.card_service;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.errors.CustomerNotFoundException;
import com.dtbbanking.card_service.mapper.CardMapper;
import com.dtbbanking.card_service.model.Card;
import com.dtbbanking.card_service.model.CardType;
import com.dtbbanking.card_service.repository.CardRepository;
import com.dtbbanking.card_service.service.CardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock private CardRepository cardRepository;
    @Mock private CardMapper cardMapper;
    @Mock private WebClient webClient;

    @InjectMocks
    private CardService cardService;

    private UUID accountId;
    private UUID cardId;
    private CardRequestDto requestDto;
    private Card card;
    private CardResponseDto responseDto;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        cardId = UUID.randomUUID();

        requestDto = new CardRequestDto();
        requestDto.setCardAlias("My Visa");
        requestDto.setAccountId(accountId);
        requestDto.setCardType(CardType.VIRTUAL);

        card = Card.builder()
                .id(cardId)
                .cardAlias("My Visa")
                .accountId(accountId)
                .cardType(CardType.VIRTUAL)
                .pan("1234567890123456")
                .cvv("123")
                .createdAt(LocalDateTime.now())
                .build();

        responseDto = new CardResponseDto();
        responseDto.setId(cardId);
        responseDto.setCardAlias("My Visa");
        responseDto.setCardType(CardType.VIRTUAL);
        responseDto.setAccountId(accountId);
        responseDto.setPan("************3456");
        responseDto.setCvv("***");
    }

    @Test
    void testCreateCardSuccess() {
        mockWebClientAccountExists();

        when(cardRepository.countByAccountId(accountId)).thenReturn(Mono.just(1L));
        when(cardRepository.existsByAccountIdAndCardType(accountId, CardType.VIRTUAL)).thenReturn(Mono.just(false));
        when(cardMapper.toEntity(requestDto)).thenReturn(card);
        when(cardRepository.save(any(Card.class))).thenReturn(Mono.just(card));
        when(cardMapper.toDto(any(Card.class))).thenReturn(responseDto);

        StepVerifier.create(cardService.createCard(requestDto))
                .expectNextMatches(resp -> resp.getCardAlias().equals("My Visa"))
                .verifyComplete();
    }

    @Test
    void testCreateCardFailsWhenAccountHasTwoCards() {
        mockWebClientAccountExists();

        when(cardRepository.countByAccountId(accountId)).thenReturn(Mono.just(2L));

        StepVerifier.create(cardService.createCard(requestDto))
                .expectErrorMatches(e -> e instanceof CustomerNotFoundException &&
                        e.getMessage().contains("Account already has 2 cards"))
                .verify();
    }

    @Test
    void testCreateCardFailsWhenDuplicateCardType() {
        mockWebClientAccountExists();

        when(cardRepository.countByAccountId(accountId)).thenReturn(Mono.just(1L));
        when(cardRepository.existsByAccountIdAndCardType(accountId, CardType.VIRTUAL)).thenReturn(Mono.just(true));

        StepVerifier.create(cardService.createCard(requestDto))
                .expectErrorMatches(e -> e instanceof CustomerNotFoundException &&
                        e.getMessage().contains("Account already has a card of this type"))
                .verify();
    }

    @Test
    void testDeleteCardSuccess() {
        when(cardRepository.findById(cardId)).thenReturn(Mono.just(card));
        when(cardRepository.deleteById(cardId)).thenReturn(Mono.empty());

        StepVerifier.create(cardService.deleteCard(cardId)).verifyComplete();
    }

    @Test
    void testDeleteCardNotFound() {
        when(cardRepository.findById(cardId)).thenReturn(Mono.empty());

        StepVerifier.create(cardService.deleteCard(cardId))
                .expectErrorMatches(e -> e instanceof ResponseStatusException &&
                        ((ResponseStatusException) e).getStatusCode() == HttpStatus.NOT_FOUND)
                .verify();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void mockWebClientAccountExists() {
        WebClient.RequestHeadersUriSpec uriSpecMock = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpecMock = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpecMock = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(uriSpecMock);
        when(uriSpecMock.uri("/{accountId}", accountId)).thenReturn(headersSpecMock);
        when(headersSpecMock.retrieve()).thenReturn(responseSpecMock);

        when(responseSpecMock.onStatus(any(), any())).thenReturn(responseSpecMock);

        when(responseSpecMock.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));
    }
}
