package com.ecommerce.pedidos.exception;

public class PedidoNaoEncontradoException extends RuntimeException {

    public PedidoNaoEncontradoException(Long id) {
        super("Pedido não encontrado: " + id);
    }
}