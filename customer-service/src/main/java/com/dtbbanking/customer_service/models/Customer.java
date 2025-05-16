package com.dtbbanking.customer_service.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("tb_customers")
public class Customer {

    @Id
    private UUID id;

    private String firstName;

    private String lastName;

    private String otherName;

    private String email;

    private String phoneNumber;

    private String nationalId;

    private LocalDateTime dateOfBirth;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}