package com.meli.consulta.jpa.repository;

import com.meli.consulta.jpa.entity.LogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogRepository extends MongoRepository<LogEntity, String> {

    List<LogEntity> findByFechaUsoBetween(LocalDateTime desde, LocalDateTime hasta);

}
