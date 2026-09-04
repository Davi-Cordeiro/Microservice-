package com.ecommerce.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {

    private Long id;
    private Long usuarioId;
    private StatusPedido status;
    private BigDecimal valorTotal;
    private LocalDateTime dataCriacao;
    private List<ItemPedido> itens;

    public Pedido(Long usuarioId, List<ItemPedido> itens) {
        this.usuarioId = usuarioId;
        this.itens = itens;
        this.dataCriacao = LocalDateTime.now();
    }

    public BigDecimal calcularTotal() {
        this.valorTotal = itens.stream()
                .map(ItemPedido::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return this.valorTotal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }
}