package com.dtbbanking.card_service.utils;

import java.security.SecureRandom;

public class CardUtils {
    private static final SecureRandom random = new SecureRandom();

    public static String generatePan() {
        StringBuilder pan = new StringBuilder("400000");
        for (int i = 0; i < 10; i++) {
            pan.append(random.nextInt(10));
        }
        return pan.toString();
    }

    public static String generateCvv() {
        int cvv = 100 + random.nextInt(900);
        return String.valueOf(cvv);
    }

    public static String maskPan(String pan) {
        if (pan.length() < 4) return "****";
        return "*".repeat(pan.length() - 4) + pan.substring(pan.length() - 4);
    }

    public static String maskCvv(String cvv) {
        return "***";
    }
}
