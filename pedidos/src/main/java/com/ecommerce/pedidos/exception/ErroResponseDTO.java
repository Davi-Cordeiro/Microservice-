package com.ecommerce.pedidos.exception;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponseDTO(
        LocalDateTime timestamp,
        int status,
        String erro,
        String mensagem,
        Map<String, String> camposInvalidos
) {}