package com.meli.consulta.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meli.consulta.dto.DatosConsultaOutDTO;
import com.meli.consulta.service.ILogService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ConsultaController.class)
public class ConsultaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ILogService iLogService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testConsultarPorCategoria() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        Mockito.when(iLogService.consultarPorCategoria(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-categoria")
                        .param("filtro", "MLA123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testConsultarPorIp() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        Mockito.when(iLogService.consultarPorIp(anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-ip")
                        .param("filtro", "192.168.0.1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testConsultarPorFechas() throws Exception {
        DatosConsultaOutDTO dto = new DatosConsultaOutDTO();
        Mockito.when(iLogService.consultarPorFechas(anyString(), anyString())).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/consultar/consumo-por-fechas")
                        .param("fechaInicial", "2025-09-01")
                        .param("fechaFinal", "2025-09-05"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

}
