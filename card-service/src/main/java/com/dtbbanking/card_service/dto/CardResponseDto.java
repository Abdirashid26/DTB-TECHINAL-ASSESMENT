package com.dtbbanking.card_service.dto;

import com.dtbbanking.card_service.model.CardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CardResponseDto {
    private UUID id;
    private String cardAlias;
    private UUID accountId;
    private CardType cardType;
    private String pan;
    private String cvv;
    private LocalDateTime createdAt;
}
