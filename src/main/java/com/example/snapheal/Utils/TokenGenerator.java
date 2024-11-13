package com.example.snapheal.Utils;

import java.security.SecureRandom;
import java.util.Random;

public class TokenGenerator {

    private static final Random RANDOM = new SecureRandom();
    public static int EXPIRATION_MINUTE = 3;

    public static String generate6DigitToken() {
        int token = 100000 + RANDOM.nextInt(900000); // Generates a number from 100000 to 999999
        return String.format("%6d", token);
    }
}