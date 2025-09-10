package com.meli.consulta.jpa.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "log_proxy")
public class LogEntity {

    @Id
    private String id;
    private String path;
    private String ip;
    private LocalDateTime fechaUso;
    private String status;

}
