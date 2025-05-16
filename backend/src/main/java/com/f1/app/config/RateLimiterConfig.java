package com.f1.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.Semaphore;

@Configuration
public class RateLimiterConfig {
    
    private static final int MAX_CONCURRENT_REQUESTS = 2;
    private static final long REQUEST_DELAY_MS = 500; // 500ms between requests
    
    @Bean
    public Semaphore apiSemaphore() {
        return new Semaphore(MAX_CONCURRENT_REQUESTS, true);
    }
    
    @Bean
    public Duration requestDelay() {
        return Duration.ofMillis(REQUEST_DELAY_MS);
    }
} 