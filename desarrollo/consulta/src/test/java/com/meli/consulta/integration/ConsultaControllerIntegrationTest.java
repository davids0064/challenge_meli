package com.meli.consulta.integration;

import com.meli.consulta.controller.ConsultaController;
import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.ILogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultaController.class)
public class ConsultaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILogService iLogService;

    @Test
    void consultarPorCategoria_deberiaRetornarListaDeDTOs() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        dto.setIp("10.0.0.1");
        dto.setPath("categoria3");
        dto.setFecha("2025-09-01");
        dto.setCount(789);
        when(iLogService.consultarPorCategoria("categoria1")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-categoria")
                        .param("filtro", "categoria1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].path").value("categoria3"))
                .andExpect(jsonPath("$[0].ip").value("10.0.0.1"))
                .andExpect(jsonPath("$[0].fecha").value("2025-09-01"))
                .andExpect(jsonPath("$[0].count").value(789));
    }

    @Test
    void consultarPorIp_deberiaRetornarListaDeDTOs() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        dto.setIp("10.0.0.1");
        dto.setPath("categoria3");
        dto.setFecha("2025-09-01");
        dto.setCount(789);
        when(iLogService.consultarPorIp("10.0.0.1")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-ip")
                        .param("filtro", "10.0.0.1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ip").value("10.0.0.1"))
                .andExpect(jsonPath("$[0].count").value(789));
    }

    @Test
    void consultarPorFechas_deberiaRetornarListaDeDTOs() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        dto.setIp("10.0.0.1");
        dto.setPath("categoria3");
        dto.setFecha("2025-09-01");
        dto.setCount(789);
        when(iLogService.consultarPorFechas("2025-09-01", "2025-09-08")).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-fechas")
                        .param("fechaInicial", "2025-09-01")
                        .param("fechaFinal", "2025-09-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fecha").value("2025-09-01"))
                .andExpect(jsonPath("$[0].count").value(789));;
    }

}
