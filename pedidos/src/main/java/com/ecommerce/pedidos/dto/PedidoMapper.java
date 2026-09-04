package com.ecommerce.pedidos.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.ecommerce.pedidos.model.ItemPedido;
import com.ecommerce.pedidos.model.Pedido;

public class PedidoMapper {

    private PedidoMapper() {
    }

    public static Pedido paraDominio(PedidoRequestDTO dto) {
        List<ItemPedido> itens = dto.itens().stream()
                .map(PedidoMapper::paraDominio)
                .collect(Collectors.toList());

        return new Pedido(dto.usuarioId(), itens);
    }

    private static ItemPedido paraDominio(ItemRequestDTO dto) {
        return new ItemPedido(dto.produtoId(), dto.quantidade(), dto.precoUnitario());
    }

    public static PedidoResponseDTO paraResponseDTO(Pedido pedido) {
        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getStatus().name(),
                pedido.getValorTotal()
        );
    }
}