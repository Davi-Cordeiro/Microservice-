package com.ecommerce.pedidos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.StatusPedido;

class PedidoMapperTest {

    @Test
    void deveConverterRequestDTOParaDominioComItensCorretos() {
        PedidoRequestDTO requestDTO = new PedidoRequestDTO(
                1L,
                List.of(
                        new ItemRequestDTO(5L, 2, new BigDecimal("49.90")),
                        new ItemRequestDTO(8L, 1, new BigDecimal("19.90"))
                )
        );

        Pedido pedido = PedidoMapper.paraDominio(requestDTO);

        assertEquals(1L, pedido.getUsuarioId());
        assertEquals(2, pedido.getItens().size());
        assertEquals(5L, pedido.getItens().get(0).getProdutoId());
    }

    @Test
    void deveConverterPedidoParaResponseDTOComStatusComoString() {
        Pedido pedido = new Pedido(1L, List.of());
        pedido.setId(99L);
        pedido.setStatus(StatusPedido.APROVADO);
        pedido.calcularTotal();

        PedidoResponseDTO responseDTO = PedidoMapper.paraResponseDTO(pedido);

        assertEquals(99L, responseDTO.pedidoId());
        assertEquals("APROVADO", responseDTO.status());
        assertEquals(0, BigDecimal.ZERO.compareTo(responseDTO.valorTotal()));
    }
}