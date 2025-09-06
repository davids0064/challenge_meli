package com.meli.proxy.jpa.repository;

import com.meli.proxy.jpa.entity.LogEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends MongoRepository<LogEntity, String> {
}
