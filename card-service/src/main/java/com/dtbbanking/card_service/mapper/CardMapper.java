package com.dtbbanking.card_service.mapper;

import com.dtbbanking.card_service.dto.CardRequestDto;
import com.dtbbanking.card_service.dto.CardResponseDto;
import com.dtbbanking.card_service.model.Card;
import org.springframework.stereotype.Component;

/**
 * Component responsible for mapping between Card entities and their corresponding DTOs.
 * <p>
 * Provides methods to convert from {@link CardRequestDto} to {@link Card} entity,
 * from {@link Card} entity to masked {@link CardResponseDto},
 * and from {@link Card} entity to unmasked {@link CardResponseDto}.
 * </p>
 */
@Component
public class CardMapper {

    /**
     * Converts a {@link CardRequestDto} to a {@link Card} entity.
     * <p>
     * Note: The {@code pan} and {@code cvv} fields are not set here,
     * as they are generated later in the service layer.
     * </p>
     *
     * @param dto the {@code CardRequestDto} to convert
     * @return a new {@code Card} entity, or {@code null} if the input dto is {@code null}
     */
    public Card toEntity(CardRequestDto dto) {
        if (dto == null) return null;

        return Card.builder()
                .cardAlias(dto.getCardAlias())
                .accountId(dto.getAccountId())
                .cardType(dto.getCardType())
                // pan and cvv are generated in service layer, so not set here
                .build();
    }

    /**
     * Converts a {@link Card} entity to a masked {@link CardResponseDto}.
     * <p>
     * Sensitive data fields such as {@code pan} and {@code cvv} are masked for security reasons.
     * </p>
     *
     * @param card the {@code Card} entity to convert
     * @return a new masked {@code CardResponseDto}, or {@code null} if the input card is {@code null}
     */
    public CardResponseDto toDto(Card card) {
        if (card == null) return null;

        return CardResponseDto.builder()
                .id(card.getId())
                .cardAlias(card.getCardAlias())
                .accountId(card.getAccountId())
                .cardType(card.getCardType())
                .pan(maskPan(card.getPan()))
                .cvv(maskCvv(card.getCvv()))
                .createdAt(card.getCreatedAt())
                .build();
    }

    /**
     * Masks the provided PAN (Primary Account Number).
     * <p>
     * Shows only the last 4 digits; all other digits are replaced with asterisks.
     * Returns "****" if the PAN is {@code null} or shorter than 4 characters.
     * </p>
     *
     * @param pan the original PAN string
     * @return the masked PAN string
     */
    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        return "**** **** **** " + pan.substring(pan.length() - 4);
    }

    /**
     * Masks the provided CVV (Card Verification Value).
     * <p>
     * Always returns "***" regardless of the input, or "***" if the CVV is {@code null}.
     * </p>
     *
     * @param cvv the original CVV string
     * @return the masked CVV string
     */
    private String maskCvv(String cvv) {
        return cvv == null ? "***" : "***";
    }

    /**
     * Converts a {@link Card} entity to an unmasked {@link CardResponseDto}.
     * <p>
     * Sensitive fields such as {@code pan} and {@code cvv} are included as-is without masking.
     * </p>
     *
     * @param card the {@code Card} entity to convert
     * @return a new unmasked {@code CardResponseDto}, or {@code null} if the input card is {@code null}
     */
    public CardResponseDto toDtoUnmasked(Card card) {
        if (card == null) return null;

        return CardResponseDto.builder()
                .id(card.getId())
                .cardAlias(card.getCardAlias())
                .accountId(card.getAccountId())
                .cardType(card.getCardType())
                .pan(card.getPan())
                .cvv(card.getCvv())
                .createdAt(card.getCreatedAt())
                .build();
    }
}