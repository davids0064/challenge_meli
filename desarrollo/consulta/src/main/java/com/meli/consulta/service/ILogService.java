package com.meli.consulta.service;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ILogService {

    ResponseEntity<List<DatosConsultaOutDTO>> consultarPorCategoria(String filtro);
    ResponseEntity<List<DatosConsultaOutDTO>> consultarPorIp(String filtro);
    ResponseEntity<List<DatosConsultaOutDTO>> consultarPorFechas(String fechaInicial, String fechaFinal);

}
