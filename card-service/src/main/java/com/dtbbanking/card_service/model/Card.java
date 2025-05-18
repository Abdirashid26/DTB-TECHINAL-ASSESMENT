package com.dtbbanking.card_service.model;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "tb_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    private UUID id;

    @Column("card_alias")
    private String cardAlias;

    @Column("account_id")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column("type")
    private CardType cardType; // VIRTUAL or PHYSICAL

    @Column("pan")
    private String pan;

    @Column("cvv")
    private String cvv;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
