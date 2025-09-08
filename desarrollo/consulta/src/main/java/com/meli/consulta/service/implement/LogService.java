package com.meli.consulta.service.implement;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.jpa.entity.LogEntity;
import com.meli.consulta.jpa.repository.LogRepository;
import com.meli.consulta.service.ILogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService implements ILogService {

    private final MongoTemplate mongoTemplate;

    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorCategoria(String filtro) {
        String regex = "/categories/" + filtro + "$";
        AggregationResults<DatosConsultaOutDTO> results = mongoTemplate.aggregate(consultarDatos(regex, "path"), "log_proxy", DatosConsultaOutDTO.class);
        return ResponseEntity.ok(results.getMappedResults());
    }

    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorIp(String filtro) {
        AggregationResults<DatosConsultaOutDTO> results = mongoTemplate.aggregate(consultarDatos(filtro, "ip"), "log_proxy", DatosConsultaOutDTO.class);
        return ResponseEntity.ok(results.getMappedResults());
    }

    private Aggregation consultarDatos(String regex, String campo) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where(campo).regex(regex)),
                Aggregation.group(campo).count().as("count"),
                Aggregation.project("count").and("_id").as("key"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"))
        );
        return aggregation;
    }

    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorFechas(String fechaInicial, String fechaFinal) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        LocalDate fechaDesde = LocalDate.parse(fechaInicial, formatter);
        LocalDate fechaHasta = LocalDate.parse(fechaFinal, formatter);
        LocalDateTime desde = fechaDesde.atStartOfDay();
        LocalDateTime hasta = fechaHasta.atTime(LocalTime.MAX);
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("fechaUso").gte(desde).lte(hasta)),
                Aggregation.match(Criteria.where("path").regex("^/categories/MLA\\d+$")),
                Aggregation.project()
                        .andExpression("dateToString('%Y-%m-%d', fechaUso)").as("fecha")
                        .and("ip").as("ip")
                        .and("path").as("path"),
                Aggregation.group(Fields.fields("fecha", "ip", "path"))
                        .count().as("count"),
                Aggregation.project("count")
                        .and("_id.fecha").as("fecha")
                        .and("_id.ip").as("ip")
                        .and("_id.path").as("path"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "fecha"))
        );

        AggregationResults<DatosConsultaOutDTO> results =
                mongoTemplate.aggregate(agg, "log_proxy", DatosConsultaOutDTO.class);

        return ResponseEntity.ok(results.getMappedResults());
    }

}
