package com.ecommerce.pedidos.service;

import org.springframework.stereotype.Service;

import com.ecommerce.pedidos.dto.PedidoMapper;
import com.ecommerce.pedidos.dto.PedidoRequestDTO;
import com.ecommerce.pedidos.dto.PedidoResponseDTO;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.StatusPedido;
import com.ecommerce.pedidos.repository.PedidoRepository;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final RegraAprovacaoService regraAprovacaoService;

    public PedidoService(PedidoRepository pedidoRepository,
                          RegraAprovacaoService regraAprovacaoService) {
        this.pedidoRepository = pedidoRepository;
        this.regraAprovacaoService = regraAprovacaoService;
    }

    public PedidoResponseDTO processar(PedidoRequestDTO requestDTO) {
        Pedido pedido = PedidoMapper.paraDominio(requestDTO);

        pedido.calcularTotal();

        boolean aprovado = regraAprovacaoService.aprovar(pedido);
        pedido.setStatus(aprovado ? StatusPedido.APROVADO : StatusPedido.RECUSADO);

        Long id = pedidoRepository.salvar(pedido);
        pedido.setId(id);

        return PedidoMapper.paraResponseDTO(pedido);
    }
}