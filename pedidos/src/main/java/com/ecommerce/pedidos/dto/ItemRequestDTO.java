package com.ecommerce.pedidos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemRequestDTO(
        @NotNull Long produtoId,
        @NotNull @Positive Integer quantidade,
        @NotNull @Positive BigDecimal precoUnitario
) {}