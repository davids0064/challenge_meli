package com.meli.consulta.controller;

import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.ILogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/consultar")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://front.localhost")
public class ConsultaController {

    private final ILogService iLogService;

    @GetMapping("consumo-por-categoria")
    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorCategoria(@RequestParam String filtro) {
        return ResponseEntity.ok(iLogService.consultarPorCategoria(filtro));
    }

    @GetMapping("consumo-por-ip")
    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorIp(@RequestParam String filtro) {
        return ResponseEntity.ok(iLogService.consultarPorIp(filtro));
    }

    @GetMapping("consumo-por-status")
    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorStatus() {
        return ResponseEntity.ok(iLogService.consultarPorStatus());
    }

    @GetMapping("consumo-por-fechas")
    public ResponseEntity<List<DatosConsultaOutDTO>> consultarPorFechas(
            @RequestParam String fechaInicial,
            @RequestParam String fechaFinal) {
        return ResponseEntity.ok(iLogService.consultarPorFechas(fechaInicial, fechaFinal));
    }

}
