package com.meli.consulta.controller;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.ILogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/consultar")
@RequiredArgsConstructor
public class ConsultaController {

    private final ILogService iLogService;

    @GetMapping("consumo-por-categoria")
    private ResponseEntity<List<DatosConsultaOutDTO>> consultarPorCategoria(@RequestParam String filtro){
        return iLogService.consultarPorCategoria(filtro);
    }

    @GetMapping("consumo-por-ip")
    private ResponseEntity<List<DatosConsultaOutDTO>> consultarPorIp(@RequestParam String filtro){
        return iLogService.consultarPorIp(filtro);
    }

    @GetMapping("consumo-por-fechas")
    private ResponseEntity<List<DatosConsultaOutDTO>> consultarPorFechas(@RequestParam String fechaInicial, @RequestParam String fechaFinal){
        return iLogService.consultarPorFechas(fechaInicial, fechaFinal);
    }

}
