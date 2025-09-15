package com.meli.proxy.filter;

import com.meli.proxy.config.RateLimitConfig;
import com.meli.proxy.service.ILogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProxyControlFilter implements GlobalFilter {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitConfig rateLimitConfig;
    private final RedisScript<Long> tokenBucketScript;
    private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private final ILogService iLogService;
    private final MeterRegistry meterRegistry;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().toString();
        String path = exchange.getRequest().getPath().value();
        String ip = resolveClientIp(exchange);
        if (isOtherPath(path)) {
            return chain.filter(exchange);
        }
        String normalizedPath = normalizePath(path);
        String key = "rate:" + ip + ":" + method + ":" + normalizedPath;
        log.error("key >>>>>> " + key);
        RateLimitConfig.Rule matchedRule = obtenerRegla(ip, normalizedPath);
        log.error("matchedRule.getIp() >>>>> " + matchedRule.getIp() + " matchedRule.getPath() >>>>> " + matchedRule.getPath());
        int replenishRate = matchedRule != null ? matchedRule.getReplenishRate() : rateLimitConfig.getDefaults().getReplenishRate();
        int burstCapacity = matchedRule != null ? matchedRule.getBurstCapacity() : rateLimitConfig.getDefaults().getBurstCapacity();
        log.error("replenishRate >>>>>> " + replenishRate);
        log.error("burstCapacity >>>>>> " + burstCapacity);
        Clock clock = Clock.systemUTC();
        String now = String.valueOf(clock.millis());
        return redisTemplate.execute(tokenBucketScript, List.of(key),
                        String.valueOf(replenishRate),
                        String.valueOf(burstCapacity),
                        now)
                .next()
                .flatMap(tokensLeft -> {
                    log.error("tokensLeft >>>>>> " + tokensLeft);
                    if (tokensLeft < 0) {
                        return manejarRechazo(exchange, ip, path, tokensLeft);
                    }
                    return manejarExito(exchange, chain, ip, path);
                });
    }

    private Mono<Void> manejarRechazo(ServerWebExchange exchange, String ip, String path, Long tokensLeft) {
        iLogService.registroLog(ip, path, HttpStatus.TOO_MANY_REQUESTS);
        log.warn("rate_limit_exceeded | ip={} | path={} | tokens={}", ip, path, tokensLeft);
        meterRegistry.counter("proxy_rate_limit_rejections", "ip", ip, "path", path).increment();
        return redisTemplate.opsForValue()
                .increment("proxy:rate_limit_rejections")
                .doOnNext(count -> log.info("📊 Rechazos acumulados: {}", count))
                .then(Mono.defer(() -> {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }));
    }

    private Mono<Void> manejarExito(ServerWebExchange exchange, GatewayFilterChain chain, String ip, String path) {
        iLogService.registroLog(ip, path, HttpStatus.OK);
        log.info("rate_limit_ok | ip={} | path={}", ip, path);
        meterRegistry.counter("proxy_requests_ok", "ip", ip, "path", path).increment();
        return redisTemplate.opsForValue()
                .increment("proxy:requests_ok")
                .doOnNext(count -> log.info("✅ Peticiones OK acumuladas: {}", count))
                .then(Mono.defer(() -> {
                    ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("meliBackend");
                    Duration timeout = Duration.ofSeconds(rateLimitConfig.getBackendTimeoutSeconds());
                    return circuitBreaker.run(
                            chain.filter(exchange)
                                    .timeout(timeout)
                                    .doOnSubscribe(sub -> log.info("🔌 Backend subscribed"))
                                    .doOnTerminate(() -> log.info("✅ Backend terminated")),
                            fallback(exchange, ip, path));
                }));
    }

    private Function<Throwable, Mono<Void>> fallback(ServerWebExchange exchange, String ip, String path) {
        return throwable -> {
            log.error("circuit_breaker_triggered | ip={} | path={} | error={}", ip, path, throwable.getMessage());
            iLogService.registroLog(ip, path, HttpStatus.SERVICE_UNAVAILABLE);
            meterRegistry.counter("proxy_activations", "ip", ip, "path", path).increment();
            return redisTemplate.opsForValue()
                    .increment("proxy:activations")
                    .doOnNext(count -> log.info("📈 Activaciones acumuladas: {}", count))
                    .then(Mono.defer(() -> {
                        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                        return exchange.getResponse().setComplete();
                    }));
        };
    }

    private String resolveClientIp(ServerWebExchange exchange) {
        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null && exchange.getRequest().getRemoteAddress() != null) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    private boolean isOtherPath(String path) {
        if(path.startsWith("/actuator") || path.startsWith("/metric")){
            return true;
        }
        return false;
    }

    private RateLimitConfig.Rule obtenerRegla(String ip, String path) {
        // Paso 1: buscar regla por IP + path
        RateLimitConfig.Rule reglaCompleta = rateLimitConfig.getRules().stream()
                .filter(rule -> {
                    boolean matchIp = rule.getIp() == null || Objects.equals(ip, rule.getIp());
                    boolean matchPath = rule.getPath() == null || path.startsWith(rule.getPath());
                    return matchIp && matchPath;
                })
                .max((r1, r2) -> Integer.compare(calcularScore(r1), calcularScore(r2)))
                .orElse(null);

        if (reglaCompleta != null) {
            return reglaCompleta;
        }

        // Paso 2: fallback por path si no hubo match por IP
        RateLimitConfig.Rule reglaPorPath = rateLimitConfig.getRules().stream()
                .filter(rule -> rule.getIp() == null && rule.getPath() != null && path.startsWith(rule.getPath()))
                .max((r1, r2) -> Integer.compare(calcularScore(r1), calcularScore(r2)))
                .orElse(null);

        if (reglaPorPath != null) {
            log.warn("⚠️ Fallback aplicado: regla por path sin IP | path={}", path);
        }

        return reglaPorPath;
    }

    private int calcularScore(RateLimitConfig.Rule rule) {
        return (rule.getIp() != null ? 2 : 0) + (rule.getPath() != null ? 1 : 0);
    }

    private String normalizePath(String path) {
        if (path.startsWith("/categories/")) {
            return "/categories/";
        }
        return path;
    }
}
