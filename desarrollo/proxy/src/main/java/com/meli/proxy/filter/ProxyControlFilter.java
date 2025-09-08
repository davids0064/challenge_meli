package com.meli.proxy.filter;

import com.meli.proxy.config.RateLimitConfig;
import com.meli.proxy.service.ILogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyControlFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ILogService iLogService;
    private final Duration WINDOW = Duration.ofMinutes(1);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String method = exchange.getRequest().getMethod().toString();
        String path = exchange.getRequest().getPath().value();
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");

        if (ip == null && exchange.getRequest().getRemoteAddress() != null) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        // Construir clave única (IP + path)
        String key = "rate:" + ip + ":" + path;
        log.info("Request received → IP: {}, Method: {}, Path: {}", ip, method, path);

        // Buscar regla que aplique
        RateLimitConfig.Rule matchedRule = obtenerReglas(ip, path);

        log.error("matchedRule.getIp() >>>>>>>> " + matchedRule.getIp());
        // Si no hay regla específica, usar defaults
        int maxRequests = (matchedRule != null) ? matchedRule.getReplenishRate() : rateLimitConfig.getDefaults().getReplenishRate();
        iLogService.registroLog(ip, path);
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        redisTemplate.expire(key, WINDOW).subscribe();
                    }
                    if (count > maxRequests) {
                        log.warn("Rate limit exceeded → IP: {}, Path: {}, Count: {}", path, count);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }
                    ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("meliBackend");
                    return circuitBreaker.run(
                            chain.filter(exchange)
                                    .timeout(Duration.ofSeconds(1))
                                    .doOnSubscribe(sub -> log.info("Subscribed to backend call"))
                                    .doOnTerminate(() -> log.info("Backend call terminated")),
                            throwable -> {
                                log.error("Circuit breaker triggered → {}", throwable.getMessage());
                                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                                return exchange.getResponse().setComplete();
                            });
                });
    }

    /**
     * @param ip
     * @param path
     * @return
     */
    private RateLimitConfig.Rule obtenerReglas(String ip, String path) {
        return rateLimitConfig.getRules().stream()
                .filter(rule -> {
                    boolean matchIp = (rule.getIp() == null || ip.equals(rule.getIp()));
                    boolean matchPath = (rule.getPath() == null || path.startsWith(rule.getPath()));
                    return matchIp && matchPath;
                })
                .max((r1, r2) -> {
                    int score1 = (r1.getIp() != null ? 1 : 0) + (r1.getPath() != null ? 1 : 0);
                    int score2 = (r2.getIp() != null ? 1 : 0) + (r2.getPath() != null ? 1 : 0);
                    return Integer.compare(score1, score2);
                })
                .orElse(null);
    }
}
