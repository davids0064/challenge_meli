package com.meli.proxy.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "rate-limiter.defaults.replenish-rate=1",
        "rate-limiter.defaults.burst-capacity=1",
        "rate-limiter.backend-timeout-seconds=2",
        "rate-limiter.rules[0].ip=127.0.0.1",
        "rate-limiter.rules[0].path=/test",
        "rate-limiter.rules[0].replenish-rate=1",
        "rate-limiter.rules[0].burst-capacity=1",
        "spring.cloud.gateway.routes[0].id=test-route",
        "spring.cloud.gateway.routes[0].uri=http://localhost:8081",
        "spring.cloud.gateway.routes[0].predicates[0]=Path=/test/**"
})
public class ProxyControlFilterIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7.2.4")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));
    }

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(options().port(8081))
            .build();

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setupStub() {
        wireMock.stubFor(get(urlEqualTo("/test"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("OK")));

        wireMock.stubFor(get(urlEqualTo("/test/failure"))
                .willReturn(aResponse()
                        .withFixedDelay(5000) // simula timeout
                        .withStatus(500)));
    }

    @Test
    void testIntegracionServicioOk() {
        webTestClient.get()
                .uri("/test")
                .header("X-Forwarded-For", "127.0.0.1")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testIntegracionManyRequests() {
        webTestClient.get()
                .uri("/test")
                .header("X-Forwarded-For", "127.0.0.1")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/test")
                .header("X-Forwarded-For", "127.0.0.1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void testIntegracionCircuitBreaker() {
        webTestClient.get()
                .uri("/test/failure")
                .header("X-Forwarded-For", "127.0.0.1")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
