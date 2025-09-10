package com.meli.consulta.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class DatosConsultaOutDTO {

    private LocalDate fecha;
    private String ip;
    private String path;
    private String status;
    private long count;

}
