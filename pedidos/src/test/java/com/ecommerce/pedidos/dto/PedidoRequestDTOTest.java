package com.ecommerce.pedidos.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class PedidoRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void deveSerValidoComDadosCorretos() {
        ItemRequestDTO item = new ItemRequestDTO(1L, 2, new BigDecimal("10.00"));
        PedidoRequestDTO dto = new PedidoRequestDTO(1L, List.of(item));

        Set<ConstraintViolation<PedidoRequestDTO>> violacoes = validator.validate(dto);

        assertTrue(violacoes.isEmpty());
    }

    @Test
    void deveSerInvalidoComListaDeItensVazia() {
        PedidoRequestDTO dto = new PedidoRequestDTO(1L, List.of());

        Set<ConstraintViolation<PedidoRequestDTO>> violacoes = validator.validate(dto);

        assertEquals(1, violacoes.size());
    }

    @Test
    void deveSerInvalidoComUsuarioIdNulo() {
        ItemRequestDTO item = new ItemRequestDTO(1L, 2, new BigDecimal("10.00"));
        PedidoRequestDTO dto = new PedidoRequestDTO(null, List.of(item));

        Set<ConstraintViolation<PedidoRequestDTO>> violacoes = validator.validate(dto);

        assertEquals(1, violacoes.size());
    }

    @Test
    void deveSerInvalidoComQuantidadeNegativa() {
        ItemRequestDTO item = new ItemRequestDTO(1L, -5, new BigDecimal("10.00"));
        PedidoRequestDTO dto = new PedidoRequestDTO(1L, List.of(item));

        Set<ConstraintViolation<PedidoRequestDTO>> violacoes = validator.validate(dto);

        assertEquals(1, violacoes.size());
    }

    @Test
    void deveSerInvalidoComPrecoUnitarioZero() {
        ItemRequestDTO item = new ItemRequestDTO(1L, 2, BigDecimal.ZERO);
        PedidoRequestDTO dto = new PedidoRequestDTO(1L, List.of(item));

        Set<ConstraintViolation<PedidoRequestDTO>> violacoes = validator.validate(dto);

        assertEquals(1, violacoes.size());
    }
}