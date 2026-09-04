package com.ecommerce.pedidos.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ecommerce.pedidos.model.Pedido;

@Service
public class RegraAprovacaoService {

    private static final BigDecimal LIMITE_APROVACAO = new BigDecimal("5000.00");

    public boolean aprovar(Pedido pedido) {
        return pedido.getValorTotal().compareTo(LIMITE_APROVACAO) <= 0;
    }
}