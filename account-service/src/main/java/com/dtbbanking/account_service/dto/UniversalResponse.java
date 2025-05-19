package com.dtbbanking.account_service.dto;

public record UniversalResponse<T>(int status, String message, T data) {
    public static <T> UniversalResponse<T> ok(T data) {
        return new UniversalResponse<>(200, "Success", data);
    }

    public static <T> UniversalResponse<T> error(int status, String message, T data) {
        return new UniversalResponse<>(status, message, data);
    }
}
