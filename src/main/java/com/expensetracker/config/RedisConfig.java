package com.expensetracker.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class RedisConfig {
    // Using simple in-memory cache (no Redis required)
    // To enable Redis later, add redis dependency config in application.yml
}
