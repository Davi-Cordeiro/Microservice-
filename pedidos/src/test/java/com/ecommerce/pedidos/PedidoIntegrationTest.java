package com.ecommerce.pedidos;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:pedidos_integration;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
class PedidoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveCriarPedidoEDepoisBuscarPeloIdRealDevolvido() throws Exception {
        String jsonRequest = """
                {
                    "usuarioId": 1,
                    "itens": [
                        { "produtoId": 5, "quantidade": 2, "precoUnitario": 49.90 }
                    ]
                }
                """;

        MvcResult resultadoCriacao = mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andReturn();

        String corpoResposta = resultadoCriacao.getResponse().getContentAsString();
        Integer pedidoId = JsonPath.read(corpoResposta, "$.pedidoId");

        mockMvc.perform(get("/api/pedidos/" + pedidoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pedidoId").value(pedidoId))
                .andExpect(jsonPath("$.status").value("APROVADO"))
                .andExpect(jsonPath("$.valorTotal").value(99.80));
    }

    @Test
    void deveCriarPedidoRecusadoQuandoValorPassaDoLimite() throws Exception {
        String jsonRequest = """
                {
                    "usuarioId": 1,
                    "itens": [
                        { "produtoId": 5, "quantidade": 200, "precoUnitario": 49.90 }
                    ]
                }
                """;

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADO"));
    }

    @Test
    void deveRetornar404AoBuscarPedidoQueNuncaFoiCriado() throws Exception {
        mockMvc.perform(get("/api/pedidos/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Pedido não encontrado"));
    }
}