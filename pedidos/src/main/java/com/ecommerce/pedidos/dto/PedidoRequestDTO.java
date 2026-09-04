package com.ecommerce.pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PedidoRequestDTO(
        @NotNull Long usuarioId,
        @NotEmpty @Valid List<ItemRequestDTO> itens
) {}