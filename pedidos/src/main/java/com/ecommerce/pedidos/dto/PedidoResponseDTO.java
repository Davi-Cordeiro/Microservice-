package com.ecommerce.pedidos.dto;

import java.math.BigDecimal;

public record PedidoResponseDTO(
        Long pedidoId,
        String status,
        BigDecimal valorTotal
) {}