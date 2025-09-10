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
        String key = "rate:" + ip + ":" + path;
        log.info("Request received → IP: {}, Method: {}, Path: {}", ip, method, path);
        RateLimitConfig.Rule matchedRule = obtenerReglas(ip, path);
        log.error("matchedRule.getIp() >>>>>>>> " + matchedRule.getIp());
        int maxRequests = (matchedRule != null) ? matchedRule.getReplenishRate() : rateLimitConfig.getDefaults().getReplenishRate();

        String finalIp = ip;
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    if (count == 1) {
                        redisTemplate.expire(key, WINDOW).subscribe();
                    }
                    log.error("count >>>>>>>>> " + count);
                    log.error("maxRequests >>>>>>>>> " + maxRequests);
                    if (count > maxRequests) {
                        iLogService.registroLog(finalIp, path, HttpStatus.TOO_MANY_REQUESTS);
                        log.error("entro a limite >>>>>>>>> " + count);
                        log.warn("Rate limit exceeded → IP: {}, Path: {}, Count: {}", path, count);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        log.warn("exchange.getResponse().getStatusCode()", exchange.getResponse().getStatusCode().value());
                        return exchange.getResponse().setComplete();
                    } else {
                        iLogService.registroLog(finalIp, path, HttpStatus.OK);
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
                                iLogService.registroLog(finalIp, path, HttpStatus.SERVICE_UNAVAILABLE);
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
