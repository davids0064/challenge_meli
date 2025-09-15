package com.meli.proxy.integration;

import com.meli.proxy.jpa.entity.LogEntity;
import com.meli.proxy.jpa.repository.LogRepository;
import com.meli.proxy.service.implement.LogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(LogService.class)
@Testcontainers
public class LogServiceIntegrationTest {

    @Autowired
    private LogService logService;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;

    @Test
    void registroLog_deberiaPersistirEntidadEnBD() {
        long cantidadAntes = 1L;
        long cantidadDespues = 2L;
        logService.registroLog("127.0.0.1", "/categories/MLA120353", HttpStatus.OK);
        List<LogEntity> logs = logRepository.findAll();
        assertThat(cantidadDespues).isEqualTo(cantidadAntes + 1);
        LogEntity log = logs.get(0);
        assertThat(log.getIp()).isEqualTo("127.0.0.1");
        assertThat(log.getPath()).isEqualTo("/categories/MLA120353");
        assertThat(log.getFechaUso()).isNotNull();
        assertThat(log.getId()).isNotBlank();
    }

}
