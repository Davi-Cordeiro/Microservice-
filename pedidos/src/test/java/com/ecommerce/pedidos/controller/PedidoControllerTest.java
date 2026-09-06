package com.ecommerce.pedidos.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.pedidos.dto.PedidoResponseDTO;
import com.ecommerce.pedidos.exception.PedidoNaoEncontradoException;
import com.ecommerce.pedidos.service.PedidoService;

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Test
    void devePostarPedidoERetornar200ComAprovado() throws Exception {
        PedidoResponseDTO resposta = new PedidoResponseDTO(1L, "APROVADO", new BigDecimal("99.80"));
        when(pedidoService.processar(any())).thenReturn(resposta);

        String jsonRequest = """
                {
                    "usuarioId": 1,
                    "itens": [
                        { "produtoId": 5, "quantidade": 2, "precoUnitario": 49.90 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(1))
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void devePostarPedidoComListaVaziaERetornar400() throws Exception {
        String jsonRequest = """
                {
                    "usuarioId": 1,
                    "itens": []
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveBuscarPedidoPorIdERetornar200() throws Exception {
        PedidoResponseDTO resposta = new PedidoResponseDTO(1L, "APROVADO", new BigDecimal("99.80"));
        when(pedidoService.buscarPorId(1L)).thenReturn(resposta);

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"));
    }

    @Test
    void deveBuscarPedidoInexistenteERetornar404() throws Exception {
        when(pedidoService.buscarPorId(999L))
                .thenThrow(new PedidoNaoEncontradoException(999L));

        mockMvc.perform(get("/api/pedidos/999"))
                .andExpect(status().isNotFound());
    }
}