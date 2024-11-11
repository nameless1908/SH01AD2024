package com.example.snapheal.service;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }

    private Bucket newBucket(String ip) {
        // Giới hạn 3 request mỗi giây cho mỗi IP
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
