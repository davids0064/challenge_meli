package com.meli.proxy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimitConfig {


    private Defaults defaults;
    private List<Rule> rules;

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
}