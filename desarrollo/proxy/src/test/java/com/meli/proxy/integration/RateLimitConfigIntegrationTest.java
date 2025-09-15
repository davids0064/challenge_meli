package com.meli.proxy.integration;

import com.meli.proxy.config.RateLimitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.TestPropertySource;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
@TestPropertySource(properties = {
        "rate-limiter.defaults.replenish-rate=10",
        "rate-limiter.defaults.burst-capacity=100",
        "rate-limiter.backend-timeout-seconds=5",
        "rate-limiter.rules[0].ip=192.168.0.1",
        "rate-limiter.rules[0].path=/api/test",
        "rate-limiter.rules[0].replenish-rate=5",
        "rate-limiter.rules[0].burst-capacity=50",
        "rate-limiter.rules[1].ip=10.0.0.1",
        "rate-limiter.rules[1].path=/api/other",
        "rate-limiter.rules[1].replenish-rate=8",
        "rate-limiter.rules[1].burst-capacity=80"
})
public class RateLimitConfigIntegrationTest {

    @Autowired
    @Qualifier("rateLimitConfig")
    private RateLimitConfig config;

    @Autowired
    private RedisScript<Long> rateLimitScript;

    @Test
    void shouldLoadDefaultsCorrectly() {
        assertEquals(10, config.getDefaults().getReplenishRate());
        assertEquals(100, config.getDefaults().getBurstCapacity());
    }

    @Test
    void shouldLoadBackendTimeout() {
        assertEquals(5, config.getBackendTimeoutSeconds());
    }

    @Test
    void shouldLoadRulesCorrectly() {
        List<RateLimitConfig.Rule> rules = config.getRules();
        assertEquals(2, rules.size());

        RateLimitConfig.Rule r1 = rules.get(0);
        assertEquals("192.168.0.1", r1.getIp());
        assertEquals("/api/test", r1.getPath());
        assertEquals(5, r1.getReplenishRate());
        assertEquals(50, r1.getBurstCapacity());

        RateLimitConfig.Rule r2 = rules.get(1);
        assertEquals("10.0.0.1", r2.getIp());
        assertEquals("/api/other", r2.getPath());
        assertEquals(8, r2.getReplenishRate());
        assertEquals(80, r2.getBurstCapacity());
    }

    @Test
    void shouldExposeRateLimitScriptBean() {
        assertNotNull(rateLimitScript);
        assertTrue(rateLimitScript.getResultType() == Long.class);
        assertTrue(rateLimitScript.getScriptAsString().contains("tokens = tokens - 1"));
    }

}
