package com.dtbbanking.card_service.dto;

import com.dtbbanking.card_service.model.CardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data

public class CardRequestDto {
    @NotBlank(message = "Card alias is required")
    private String cardAlias;

    @NotNull(message = "Account ID is required")
    private UUID accountId;

    @NotNull(message = "Card type is required")
    private CardType cardType;
}
