package com.ecommerce.pedidos.model;

import java.math.BigDecimal;

public class ItemPedido {

    private Long produtoId;
    private Integer quantidade;
    private BigDecimal precoUnitario;

    public ItemPedido(Long produtoId, Integer quantidade, BigDecimal precoUnitario) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }
}