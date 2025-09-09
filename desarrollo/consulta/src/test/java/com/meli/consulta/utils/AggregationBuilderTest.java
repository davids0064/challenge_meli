package com.meli.consulta.utils;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AggregationBuilderTest {

    @Test
    void testBuildWithRegexOnly() {
        Aggregation agg = AggregationBuilder.build("^/categories/MLA\\d+$", "path", null, null, true);
        List<AggregationOperation> ops = agg.getPipeline().getOperations();

        assertEquals(5, ops.size());
        assertTrue(ops.get(0) instanceof MatchOperation);
        assertTrue(ops.get(1) instanceof ProjectionOperation);
        assertTrue(ops.get(2) instanceof GroupOperation);
        assertTrue(ops.get(3) instanceof ProjectionOperation);
        assertTrue(ops.get(4) instanceof SortOperation);
    }

    @Test
    void testBuildWithFechasAndRegex() {
        LocalDateTime desde = LocalDateTime.of(2025, 9, 1, 0, 0);
        LocalDateTime hasta = LocalDateTime.of(2025, 9, 5, 23, 59);
        Aggregation agg = AggregationBuilder.build("^/categories/MLA\\d+$", "path", desde, hasta, false);
        List<AggregationOperation> ops = agg.getPipeline().getOperations();

        assertEquals(6, ops.size());
        assertTrue(ops.get(0) instanceof MatchOperation); // regex
        assertTrue(ops.get(1) instanceof MatchOperation); // fechas
        assertTrue(ops.get(5) instanceof SortOperation);
    }

    @Test
    void testBuildWithoutRegexOrFechas() {
        Aggregation agg = AggregationBuilder.build(null, null, null, null, true);
        List<AggregationOperation> ops = agg.getPipeline().getOperations();

        assertEquals(4, ops.size());
        assertTrue(ops.get(0) instanceof ProjectionOperation);
        assertTrue(ops.get(1) instanceof GroupOperation);
        assertTrue(ops.get(2) instanceof ProjectionOperation);
        assertTrue(ops.get(3) instanceof SortOperation);
    }

}
