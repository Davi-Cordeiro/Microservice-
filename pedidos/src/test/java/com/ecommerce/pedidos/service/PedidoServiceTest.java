package com.ecommerce.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.pedidos.dto.ItemRequestDTO;
import com.ecommerce.pedidos.dto.PedidoRequestDTO;
import com.ecommerce.pedidos.dto.PedidoResponseDTO;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.repository.PedidoRepository;

class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private RegraAprovacaoService regraAprovacaoService;
    private PedidoService pedidoService;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        regraAprovacaoService = mock(RegraAprovacaoService.class);
        pedidoService = new PedidoService(pedidoRepository, regraAprovacaoService);
    }

    @Test
    void deveProcessarPedidoAprovadoESalvarComStatusCorreto() {
        PedidoRequestDTO requestDTO = new PedidoRequestDTO(
                1L,
                List.of(new ItemRequestDTO(5L, 2, new BigDecimal("49.90")))
        );

        when(regraAprovacaoService.aprovar(any(Pedido.class))).thenReturn(true);
        when(pedidoRepository.salvar(any(Pedido.class))).thenReturn(42L);

        PedidoResponseDTO resposta = pedidoService.processar(requestDTO);

        assertEquals(42L, resposta.pedidoId());
        assertEquals("APROVADO", resposta.status());
        assertEquals(0, new BigDecimal("99.80").compareTo(resposta.valorTotal()));
    }

    @Test
    void deveProcessarPedidoRecusadoESalvarComStatusCorreto() {
        PedidoRequestDTO requestDTO = new PedidoRequestDTO(
                1L,
                List.of(new ItemRequestDTO(5L, 200, new BigDecimal("49.90")))
        );

        when(regraAprovacaoService.aprovar(any(Pedido.class))).thenReturn(false);
        when(pedidoRepository.salvar(any(Pedido.class))).thenReturn(43L);

        PedidoResponseDTO resposta = pedidoService.processar(requestDTO);

        assertEquals("RECUSADO", resposta.status());
    }

    @Test
    void deveChamarRegraAprovacaoApenasUmaVez() {
        PedidoRequestDTO requestDTO = new PedidoRequestDTO(
                1L,
                List.of(new ItemRequestDTO(5L, 1, new BigDecimal("10.00")))
        );

        when(regraAprovacaoService.aprovar(any(Pedido.class))).thenReturn(true);
        when(pedidoRepository.salvar(any(Pedido.class))).thenReturn(1L);

        pedidoService.processar(requestDTO);

        verify(regraAprovacaoService).aprovar(any(Pedido.class));
    }

    @Test
    void deveSalvarPedidoComValorTotalJaCalculadoAntesDeChamarRepository() {
        PedidoRequestDTO requestDTO = new PedidoRequestDTO(
                1L,
                List.of(new ItemRequestDTO(5L, 3, new BigDecimal("10.00")))
        );

        when(regraAprovacaoService.aprovar(any(Pedido.class))).thenReturn(true);
        when(pedidoRepository.salvar(any(Pedido.class))).thenAnswer(invocation -> {
            Pedido pedidoPassado = invocation.getArgument(0);
            assertEquals(0, new BigDecimal("30.00").compareTo(pedidoPassado.getValorTotal()));
            return 1L;
        });

        pedidoService.processar(requestDTO);
    }
}