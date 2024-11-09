package com.example.snapheal.Utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;

public class DateTimeUtils {

    // Extension-like method to convert LocalDateTime to Unix timestamp (in milliseconds)
    public static long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            throw new IllegalArgumentException("LocalDateTime cannot be null");
        }
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC); // Convert to Instant
        return instant.toEpochMilli(); // Return Unix timestamp in milliseconds
    }
}

