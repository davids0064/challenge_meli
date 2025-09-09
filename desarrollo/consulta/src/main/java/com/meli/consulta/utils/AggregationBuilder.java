package com.meli.consulta.utils;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.query.Criteria;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AggregationBuilder {

    public static Aggregation build(String regex, String campoFiltro, LocalDateTime desde, LocalDateTime hasta, boolean sortByCountDesc) {
        List<AggregationOperation> ops = new ArrayList<>();

        if (regex != null && campoFiltro != null) {
            ops.add(Aggregation.match(Criteria.where(campoFiltro).regex(regex)));
        }

        if (desde != null && hasta != null) {
            ops.add(Aggregation.match(Criteria.where("fechaUso").gte(desde).lte(hasta)));
        }

        ops.add(Aggregation.project()
                .andExpression("dateToString('%Y-%m-%d', fechaUso)").as("fecha")
                .and("ip").as("ip")
                .and("path").as("path"));

        ops.add(Aggregation.group(Fields.fields("fecha", "ip", "path"))
                .count().as("count"));

        ops.add(Aggregation.project("count")
                .and("_id.fecha").as("fecha")
                .and("_id.ip").as("ip")
                .and("_id.path").as("path"));

        Sort sort = sortByCountDesc ? Sort.by(Sort.Direction.DESC, "count") : Sort.by(Sort.Direction.ASC, "fecha");
        ops.add(Aggregation.sort(sort));

        return Aggregation.newAggregation(ops);
    }

}
