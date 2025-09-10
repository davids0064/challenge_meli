package com.meli.proxy.filter;

import com.meli.proxy.config.RateLimitConfig;
import com.meli.proxy.service.ILogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Function;

import static org.mockito.Mockito.*;

public class ProxyControlFilterTest {

    private ReactiveStringRedisTemplate redisTemplate;
    private RateLimitConfig rateLimitConfig;
    private ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    private ILogService iLogService;
    private ProxyControlFilter filter;

    private ServerWebExchange exchange;
    private GatewayFilterChain chain;
    private ReactiveValueOperations<String, String> valueOps;
    private ServerHttpRequest request;
    private ServerHttpResponse response;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(ReactiveStringRedisTemplate.class);
        valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        rateLimitConfig = new RateLimitConfig();
        RateLimitConfig.Rule specificRule = new RateLimitConfig.Rule();
        specificRule.setIp("127.0.0.1");
        specificRule.setPath("/api");
        specificRule.setReplenishRate(3);
        rateLimitConfig.setRules(List.of(specificRule));

        RateLimitConfig.Defaults defaultRule = new RateLimitConfig.Defaults();
        defaultRule.setReplenishRate(5);
        rateLimitConfig.setDefaults(defaultRule);

        circuitBreakerFactory = mock(ReactiveCircuitBreakerFactory.class);
        iLogService = mock(ILogService.class);
        filter = new ProxyControlFilter(redisTemplate, rateLimitConfig, circuitBreakerFactory, iLogService);

        exchange = mock(ServerWebExchange.class);
        chain = mock(GatewayFilterChain.class);
        request = mock(ServerHttpRequest.class);
        response = mock(ServerHttpResponse.class);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        RequestPath requestPath = mock(RequestPath.class);
        when(requestPath.value()).thenReturn("/api/test");
        when(request.getPath()).thenReturn(requestPath);

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Forwarded-For", "127.0.0.1");
        when(request.getHeaders()).thenReturn(headers);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getResponse()).thenReturn(response);
    }


    @Test
    void testWithinRateLimitAndCircuitBreakerSuccess() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        ReactiveCircuitBreaker breaker = mock(ReactiveCircuitBreaker.class);
        when(circuitBreakerFactory.create(anyString())).thenReturn(breaker);
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        when(breaker.run(any(Mono.class), any(Function.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(chain).filter(exchange);
    }

    @Test
    void testCircuitBreakerFallback() {
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any())).thenReturn(Mono.just(true));

        ReactiveCircuitBreaker breaker = mock(ReactiveCircuitBreaker.class);
        when(circuitBreakerFactory.create(anyString())).thenReturn(breaker);
        when(chain.filter(exchange)).thenReturn(Mono.error(new RuntimeException("Backend error")));

        when(response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE)).thenReturn(true);
        when(response.setComplete()).thenReturn(Mono.empty());

        when(breaker.run(any(Mono.class), any(Function.class)))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Function<Throwable, Mono<Void>> fallback = (Function<Throwable, Mono<Void>>) invocation.getArgument(1);
                    return fallback.apply(new RuntimeException("Backend error"));
                });

        Mono<Void> result = filter.filter(exchange, chain);

        StepVerifier.create(result)
                .verifyComplete();

        verify(response).setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        verify(response).setComplete();
    }
}
