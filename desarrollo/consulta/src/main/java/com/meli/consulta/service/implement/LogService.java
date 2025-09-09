package com.meli.consulta.service.implement;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.ILogService;
import com.meli.consulta.utils.AggregationBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogService implements ILogService {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<DatosConsultaOutDTO> consultarPorCategoria(String filtro) {
        String regex = "/categories/" + filtro + "$";
        log.info("Consulta por categoría: {}", regex);
        var agg = AggregationBuilder.build(regex, "path", null, null, true);
        return mongoTemplate.aggregate(agg, "log_proxy", DatosConsultaOutDTO.class).getMappedResults();
    }

    @Override
    public List<DatosConsultaOutDTO> consultarPorIp(String filtro) {
        log.info("Consulta por IP: {}", filtro);
        var agg = AggregationBuilder.build(filtro, "ip", null, null, true);
        return mongoTemplate.aggregate(agg, "log_proxy", DatosConsultaOutDTO.class).getMappedResults();
    }

    @Override
    public List<DatosConsultaOutDTO> consultarPorFechas(String fechaInicial, String fechaFinal) {
        log.info("Consulta por fechas: {} - {}", fechaInicial, fechaFinal);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDateTime desde = LocalDate.parse(fechaInicial, formatter).atStartOfDay();
        LocalDateTime hasta = LocalDate.parse(fechaFinal, formatter).atTime(LocalTime.MAX);
        String regexPath = "^/categories/MLA\\d+$";
        var agg = AggregationBuilder.build(regexPath, "path", desde, hasta, false);
        return mongoTemplate.aggregate(agg, "log_proxy", DatosConsultaOutDTO.class).getMappedResults();
    }

}
