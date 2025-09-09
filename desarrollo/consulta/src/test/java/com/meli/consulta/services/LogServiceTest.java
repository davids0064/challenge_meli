package com.meli.consulta.services;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.implement.LogService;
import com.meli.consulta.utils.AggregationBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LogServiceTest {

    private MongoTemplate mongoTemplate;
    private LogService logService;

    @BeforeEach
    void setUp() {
        mongoTemplate = mock(MongoTemplate.class);
        logService = new LogService(mongoTemplate);
    }

    @Test
    void testConsultarPorCategoria() {
        String filtro = "MLA123";
        String expectedRegex = "/categories/" + filtro + "$";

        List<DatosConsultaOutDTO> mockResults = List.of(new DatosConsultaOutDTO());
        AggregationResults<DatosConsultaOutDTO> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(mockResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class)))
                .thenReturn(aggregationResults);

        List<DatosConsultaOutDTO> result = logService.consultarPorCategoria(filtro);

        assertEquals(1, result.size());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class));
    }

    @Test
    void testConsultarPorIp() {
        String filtro = "192.168.0.1";

        List<DatosConsultaOutDTO> mockResults = List.of(new DatosConsultaOutDTO());
        AggregationResults<DatosConsultaOutDTO> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(mockResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class)))
                .thenReturn(aggregationResults);

        List<DatosConsultaOutDTO> result = logService.consultarPorIp(filtro);

        assertEquals(1, result.size());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class));
    }

    @Test
    void testConsultarPorFechas() {
        String fechaInicial = "2025-09-01";
        String fechaFinal = "2025-09-05";

        List<DatosConsultaOutDTO> mockResults = List.of(new DatosConsultaOutDTO());
        AggregationResults<DatosConsultaOutDTO> aggregationResults = mock(AggregationResults.class);
        when(aggregationResults.getMappedResults()).thenReturn(mockResults);
        when(mongoTemplate.aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class)))
                .thenReturn(aggregationResults);

        List<DatosConsultaOutDTO> result = logService.consultarPorFechas(fechaInicial, fechaFinal);

        assertEquals(1, result.size());
        verify(mongoTemplate).aggregate(any(Aggregation.class), eq("log_proxy"), eq(DatosConsultaOutDTO.class));
    }

}
