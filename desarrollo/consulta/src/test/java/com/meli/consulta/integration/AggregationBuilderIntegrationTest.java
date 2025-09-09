package com.meli.consulta.integration;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.utils.AggregationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Testcontainers
public class AggregationBuilderIntegrationTest {

    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    static {
        mongoDBContainer.start();
    }

    @DynamicPropertySource
    static void mongoProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection("log_proxy");

        // Insertamos dos documentos con fechaUso como LocalDateTime
        mongoTemplate.save(new LogProxyDocument("/categories/MLA1234", "192.168.0.1", LocalDateTime.of(2025, 9, 8, 10, 0)), "log_proxy");
        mongoTemplate.save(new LogProxyDocument("/categories/MLA1234", "192.168.0.1", LocalDateTime.of(2025, 9, 8, 11, 0)), "log_proxy");
    }

    @Test
    void build_deberiaAgruparYContarPorFechaIpPath() {
        var agg = AggregationBuilder.build(
                "^/categories/MLA1234$", "path",
                LocalDateTime.of(2025, 9, 1, 0, 0),
                LocalDateTime.of(2025, 9, 8, 23, 59),
                true
        );

        List<DatosConsultaOutDTO> resultados = mongoTemplate.aggregate(agg, "log_proxy", DatosConsultaOutDTO.class).getMappedResults();

        assertThat(resultados).hasSize(1);
        assertThat(resultados.get(0).getCount()).isEqualTo(2);
        assertThat(resultados.get(0).getFecha()).isEqualTo(LocalDate.of(2025, 9, 8));
        assertThat(resultados.get(0).getIp()).isEqualTo("192.168.0.1");
        assertThat(resultados.get(0).getPath()).isEqualTo("/categories/MLA1234");
    }

    // Documento auxiliar para simular la colección original
    static class LogProxyDocument {
        private String path;
        private String ip;
        private LocalDateTime fechaUso;

        public LogProxyDocument(String path, String ip, LocalDateTime fechaUso) {
            this.path = path;
            this.ip = ip;
            this.fechaUso = fechaUso;
        }

        public String getPath() { return path; }
        public String getIp() { return ip; }
        public LocalDateTime getFechaUso() { return fechaUso; }
    }

}
