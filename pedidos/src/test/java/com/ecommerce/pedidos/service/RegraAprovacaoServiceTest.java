package com.ecommerce.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ecommerce.pedidos.model.ItemPedido;
import com.ecommerce.pedidos.model.Pedido;

class RegraAprovacaoServiceTest {

    private final RegraAprovacaoService regraAprovacaoService = new RegraAprovacaoService();

    @Test
    void deveAprovarPedidoComValorAbaixoDoLimite() {
        Pedido pedido = criarPedidoComValor(new BigDecimal("100.00"));

        assertTrue(regraAprovacaoService.aprovar(pedido));
    }

    @Test
    void deveAprovarPedidoComValorExatamenteNoLimite() {
        Pedido pedido = criarPedidoComValor(new BigDecimal("5000.00"));

        assertTrue(regraAprovacaoService.aprovar(pedido));
    }

    @Test
    void deveRecusarPedidoComValorAcimaDoLimite() {
        Pedido pedido = criarPedidoComValor(new BigDecimal("5000.01"));

        assertFalse(regraAprovacaoService.aprovar(pedido));
    }

    private Pedido criarPedidoComValor(BigDecimal valor) {
        ItemPedido item = new ItemPedido(1L, 1, valor);
        Pedido pedido = new Pedido(1L, List.of(item));
        pedido.calcularTotal();
        return pedido;
    }
}