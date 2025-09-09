package com.meli.consulta.service;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import java.util.List;

public interface ILogService {

    List<DatosConsultaOutDTO> consultarPorCategoria(String filtro);
    List<DatosConsultaOutDTO> consultarPorIp(String filtro);
    List<DatosConsultaOutDTO> consultarPorFechas(String fechaInicial, String fechaFinal);

}
