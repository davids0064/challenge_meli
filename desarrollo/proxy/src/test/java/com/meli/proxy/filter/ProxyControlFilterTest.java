package com.meli.proxy.filter;

import com.meli.proxy.config.RateLimitConfig;
import com.meli.proxy.service.ILogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;
import java.time.Clock;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProxyControlFilterTest {

    @Mock
    ReactiveStringRedisTemplate redisTemplate;
    @Mock
    RateLimitConfig rateLimitConfig;
    @Mock
    RateLimitConfig.Defaults defaults;
    @Mock
    RedisScript<Long> tokenBucketScript;
    @Mock
    ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;
    @Mock
    ReactiveCircuitBreaker circuitBreaker;
    @Mock
    ILogService iLogService;
    @Mock(strictness = Mock.Strictness.LENIENT)
    MeterRegistry meterRegistry;
    @Mock
    ServerWebExchange exchange;
    @Mock
    GatewayFilterChain chain;
    @Mock
    ServerHttpRequest request;
    @Mock
    ServerHttpResponse response;
    @Mock
    RequestPath requestPath;
    @Mock
    HttpHeaders httpHeaders;

    ProxyControlFilter filter;

    @BeforeEach
    void setup() {
        filter = new ProxyControlFilter(redisTemplate, rateLimitConfig, tokenBucketScript, circuitBreakerFactory, iLogService, meterRegistry);
    }

    @Test
    void pathExcluidoTest() {
        when(exchange.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(request.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress("127.0.0.1", 8080));
        when(requestPath.value()).thenReturn("/actuator/health");
        when(chain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(exchange);
        verifyNoInteractions(redisTemplate, iLogService);
    }

    @Test
    void limiteTokenTest() {
        String ip = "127.0.0.1";
        String path = "/api/test";

        RateLimitConfig.Rule rule = new RateLimitConfig.Rule();
        rule.setIp(ip);
        rule.setPath(path);

        when(exchange.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn(path);
        when(request.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 8080));

        when(rateLimitConfig.getRules()).thenReturn(List.of(rule));

        doAnswer(invocation -> Flux.just(-1L))
                .when(redisTemplate)
                .execute(eq(tokenBucketScript), anyList(), anyString(), anyString(), anyString());

        Counter mockCounter = mock(Counter.class);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(mockCounter);

        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));

        when(exchange.getResponse()).thenReturn(response);
        when(response.setComplete()).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(iLogService).registroLog(ip, path, HttpStatus.TOO_MANY_REQUESTS);
        verify(response).setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
        verify(response).setComplete();
    }

    @Test
    void testFilter_success_shouldProceedToBackend() {
        String ip = "127.0.0.1";
        String path = "/api/test";

        RateLimitConfig.Rule rule = new RateLimitConfig.Rule();
        rule.setIp(ip);
        rule.setPath(path);

        // Mock de request
        when(exchange.getRequest()).thenReturn(request);
        when(request.getMethod()).thenReturn(HttpMethod.GET);
        when(request.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn(path);
        when(request.getHeaders()).thenReturn(httpHeaders);
        when(httpHeaders.getFirst("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddress()).thenReturn(new InetSocketAddress(ip, 8080));

        // Configuración de reglas
        when(rateLimitConfig.getRules()).thenReturn(List.of(rule));

        // Redis devuelve tokens disponibles
        doAnswer(invocation -> Flux.just(1L))
                .when(redisTemplate)
                .execute(eq(tokenBucketScript), anyList(), anyString(), anyString(), anyString());

        // Mock de métricas
        Counter mockCounter = mock(Counter.class);
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(mockCounter);

        // Mock de Redis ValueOperations
        ReactiveValueOperations<String, String> valueOps = mock(ReactiveValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(Mono.just(1L));

        // Mock de circuit breaker
        when(circuitBreakerFactory.create("meliBackend")).thenReturn(circuitBreaker);
        when(chain.filter(exchange)).thenReturn(Mono.empty());
        when(circuitBreaker.run(any(Mono.class), any())).thenAnswer(inv -> ((Mono<?>) inv.getArgument(0)));

        // Ejecutar y verificar
        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(iLogService).registroLog(ip, path, HttpStatus.OK);
        verify(mockCounter).increment();
        verify(valueOps).increment("proxy:requests_ok");
        verify(chain).filter(exchange);
    }
}
