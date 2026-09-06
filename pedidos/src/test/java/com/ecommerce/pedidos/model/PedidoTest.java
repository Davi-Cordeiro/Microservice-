package com.ecommerce.pedidos.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;

class PedidoTest {

    @Test
    void itemPedidoDeveCalcularSubtotalCorretamente() {
        ItemPedido item = new ItemPedido(1L, 3, new BigDecimal("10.50"));

        assertEquals(new BigDecimal("31.50"), item.subtotal());
    }

    @Test
    void pedidoComUmItemDeveCalcularTotalIgualAoSubtotal() {
        ItemPedido item = new ItemPedido(1L, 2, new BigDecimal("49.90"));
        Pedido pedido = new Pedido(1L, List.of(item));

        BigDecimal total = pedido.calcularTotal();

        assertEquals(new BigDecimal("99.80"), total);
    }

    @Test
    void pedidoComMultiplosItensDeveSomarTodosOsSubtotais() {
        ItemPedido item1 = new ItemPedido(1L, 2, new BigDecimal("49.90"));
        ItemPedido item2 = new ItemPedido(2L, 1, new BigDecimal("19.90"));
        Pedido pedido = new Pedido(1L, List.of(item1, item2));

        BigDecimal total = pedido.calcularTotal();

        assertEquals(new BigDecimal("119.70"), total);
    }

    @Test
    void calcularTotalDeveAtualizarOCampoValorTotalDoPedido() {
        ItemPedido item = new ItemPedido(1L, 1, new BigDecimal("10.00"));
        Pedido pedido = new Pedido(1L, List.of(item));

        pedido.calcularTotal();

        assertEquals(new BigDecimal("10.00"), pedido.getValorTotal());
    }
}