package com.f1.app.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory redisConnectionFactory;

    public CustomHealthIndicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();

        try {
            redisConnectionFactory.getConnection().ping();
            builder.up()
                  .withDetail("redis", "Redis connection is working")
                  .withDetail("redis_host", System.getenv("REDIS_HOST"))
                  .withDetail("redis_port", System.getenv("REDIS_PORT"));
        } catch (Exception e) {
            builder.down()
                  .withDetail("redis_error", e.getMessage())
                  .withDetail("redis_host", System.getenv("REDIS_HOST"))
                  .withDetail("redis_port", System.getenv("REDIS_PORT"));
        }

        return builder.build();
    }
} 