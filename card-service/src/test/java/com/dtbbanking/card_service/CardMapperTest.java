package com.dtbbanking.card_service;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.mapper.CardMapper;
import com.dtbbanking.card_service.model.Card;
import com.dtbbanking.card_service.model.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardMapperTest {

    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        cardMapper = new CardMapper();
    }

    @Test
    void toEntity_shouldMapDtoToEntity() {
        UUID accountId = UUID.randomUUID();
        CardRequestDto dto = new CardRequestDto();
        dto.setCardAlias("TestAlias");
        dto.setAccountId(accountId);
        dto.setCardType(CardType.PHYSICAL);

        Card card = cardMapper.toEntity(dto);

        assertNotNull(card);
        assertEquals("TestAlias", card.getCardAlias());
        assertEquals(accountId, card.getAccountId());
        assertEquals(CardType.PHYSICAL, card.getCardType());
        assertNull(card.getPan());
        assertNull(card.getCvv());
    }

    @Test
    void toDto_shouldMapEntityToDtoWithMaskedPanAndCvv() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Card card = Card.builder()
                .id(id)
                .cardAlias("MyCard")
                .accountId(accountId)
                .cardType(CardType.VIRTUAL)
                .pan("1234567812345678")
                .cvv("123")
                .createdAt(createdAt)
                .build();

        CardResponseDto dto = cardMapper.toDto(card);

        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals("MyCard", dto.getCardAlias());
        assertEquals(accountId, dto.getAccountId());
        assertEquals(CardType.VIRTUAL, dto.getCardType());
        assertEquals("**** **** **** 5678", dto.getPan());
        assertEquals("***", dto.getCvv());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void toDtoUnmasked_shouldMapEntityToDtoWithoutMasking() {
        UUID id = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        Card card = Card.builder()
                .id(id)
                .cardAlias("UnmaskedCard")
                .accountId(accountId)
                .cardType(CardType.PHYSICAL)
                .pan("4321432143214321")
                .cvv("999")
                .createdAt(createdAt)
                .build();

        CardResponseDto dto = cardMapper.toDtoUnmasked(card);

        assertNotNull(dto);
        assertEquals("4321432143214321", dto.getPan());
        assertEquals("999", dto.getCvv());
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        assertNull(cardMapper.toEntity(null));
    }

    @Test
    void toDto_shouldReturnNull_whenCardIsNull() {
        assertNull(cardMapper.toDto(null));
    }

    @Test
    void toDtoUnmasked_shouldReturnNull_whenCardIsNull() {
        assertNull(cardMapper.toDtoUnmasked(null));
    }

    @Test
    void toDto_shouldHandleShortPanGracefully() {
        Card card = Card.builder()
                .pan("123")
                .cvv("321")
                .build();

        CardResponseDto dto = cardMapper.toDto(card);

        assertEquals("****", dto.getPan());
        assertEquals("***", dto.getCvv());
    }
}
