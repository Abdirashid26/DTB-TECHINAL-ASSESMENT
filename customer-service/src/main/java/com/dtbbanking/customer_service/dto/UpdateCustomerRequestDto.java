package com.dtbbanking.customer_service.dto;

import lombok.Data;

@Data
public class UpdateCustomerRequestDto {
    private String firstName;
    private String lastName;
    private String otherName;
}