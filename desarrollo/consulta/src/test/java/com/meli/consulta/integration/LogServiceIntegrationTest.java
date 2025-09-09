package com.meli.consulta.integration;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.implement.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class LogServiceIntegrationTest {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = new LogService(mongoTemplate);
        mongoTemplate.dropCollection("log_proxy");

        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        dto.setIp("192.168.0.1");
        dto.setPath("/categories/MLA1234");
        dto.setFecha(LocalDate.of(2025, 9, 8));
        dto.setCount(100);
        mongoTemplate.save(dto, "log_proxy");
    }

    @Test
    void consultarPorCategoria_deberiaRetornarResultados() {
        List<DatosConsultaOutDTO> resultados = logService.consultarPorCategoria("MLA1234");
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getPath()).isEqualTo("/categories/MLA1234");
    }

    @Test
    void consultarPorIp_deberiaRetornarResultados() {
        List<DatosConsultaOutDTO> resultados = logService.consultarPorIp("192.168.0.1");
        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getIp()).isEqualTo("192.168.0.1");
    }

}
