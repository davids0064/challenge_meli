package com.meli.proxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitConfig {


    private Defaults defaults;
    private List<Rule> rules;
    private int backendTimeoutSeconds = 3;

    @Data
    public static class Defaults {
        private int replenishRate;
        private int burstCapacity;
    }

    @Data
    public static class Rule {
        private String ip;
        private String path;
        private int replenishRate;
        private int burstCapacity;
    }


    @Bean
    public RedisScript<Long> rateLimitScript() {
        String script = """
                -- KEYS[1] = clave del bucket (ej. rate:IP:PATH)
                -- ARGV[1] = replenishRate (tokens por segundo)
                -- ARGV[2] = burstCapacity (máximo acumulable)
                -- ARGV[3] = timestamp actual en milisegundos

                local rate = tonumber(ARGV[1])
                local capacity = tonumber(ARGV[2])
                local now = tonumber(ARGV[3])

                local bucket = redis.call("HMGET", KEYS[1], "tokens", "timestamp")
                local tokens = tonumber(bucket[1])
                local last = tonumber(bucket[2])

                -- Inicialización segura si el bucket no existe o expiró
                if tokens == nil or last == nil then
                  tokens = capacity - 1
                  last = now
                  redis.call("HMSET", KEYS[1], "tokens", tokens, "timestamp", now)
                  redis.call("EXPIRE", KEYS[1], 3600)
                  return tokens
                end

                -- Refill por segundo
                local delta = math.max(0, now - last)
                local refill = 0
                if delta >= 60000 then
                  refill = rate
                  last = now
                end
                tokens = math.min(capacity, tokens + refill)

                -- Verificación de disponibilidad
                if tokens < 1 then
                  redis.call("HMSET", KEYS[1], "tokens", tokens, "timestamp", last)
                  redis.call("EXPIRE", KEYS[1], 300)
                  return -1
                else
                  tokens = tokens - 1
                  redis.call("HMSET", KEYS[1], "tokens", tokens, "timestamp", now)
                  redis.call("EXPIRE", KEYS[1], 300)
                  return tokens
                end
                                """;
        return RedisScript.of(script, Long.class);
    }
}