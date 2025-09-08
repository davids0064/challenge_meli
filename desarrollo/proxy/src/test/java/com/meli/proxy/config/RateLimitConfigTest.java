package com.meli.proxy.config;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitConfigTest {

    @Test
    void shouldSetAndGetDefaults() {
        RateLimitConfig config = new RateLimitConfig();
        RateLimitConfig.Defaults defaults = new RateLimitConfig.Defaults();
        defaults.setReplenishRate(10);
        defaults.setBurstCapacity(100);

        config.setDefaults(defaults);

        assertEquals(10, config.getDefaults().getReplenishRate());
        assertEquals(100, config.getDefaults().getBurstCapacity());
    }

    @Test
    void shouldSetAndGetRules() {
        RateLimitConfig config = new RateLimitConfig();

        RateLimitConfig.Rule rule1 = new RateLimitConfig.Rule();
        rule1.setIp("192.168.0.1");
        rule1.setPath("/api/test");
        rule1.setReplenishRate(5);
        rule1.setBurstCapacity(50);

        RateLimitConfig.Rule rule2 = new RateLimitConfig.Rule();
        rule2.setIp("10.0.0.1");
        rule2.setPath("/api/other");
        rule2.setReplenishRate(8);
        rule2.setBurstCapacity(80);

        config.setRules(List.of(rule1, rule2));

        List<RateLimitConfig.Rule> rules = config.getRules();
        assertEquals(2, rules.size());

        assertEquals("192.168.0.1", rules.get(0).getIp());
        assertEquals("/api/test", rules.get(0).getPath());
        assertEquals(5, rules.get(0).getReplenishRate());
        assertEquals(50, rules.get(0).getBurstCapacity());

        assertEquals("10.0.0.1", rules.get(1).getIp());
        assertEquals("/api/other", rules.get(1).getPath());
        assertEquals(8, rules.get(1).getReplenishRate());
        assertEquals(80, rules.get(1).getBurstCapacity());
    }

}
