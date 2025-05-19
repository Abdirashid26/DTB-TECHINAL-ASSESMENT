package com.dtbbanking.customer_service.errors;

public class GlobalException extends RuntimeException {
    public GlobalException(String message) {
        super(message);
    }
}