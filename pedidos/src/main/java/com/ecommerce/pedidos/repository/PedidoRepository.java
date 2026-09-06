package com.ecommerce.pedidos.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.sql.DataSource;

import org.springframework.stereotype.Repository;

import com.ecommerce.pedidos.model.ItemPedido;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.StatusPedido;

import java.util.ArrayList;
import java.util.Optional;

@Repository
public class PedidoRepository {

    private final DataSource dataSource;

    public PedidoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long salvar(Pedido pedido) {
        String sqlPedido =
                "INSERT INTO pedido (usuario_id, status, valor_total, data_criacao) " +
                "VALUES (?, ?, ?, ?)";

        String sqlItem =
                "INSERT INTO item_pedido " +
                "(pedido_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psPedido = conn.prepareStatement(
                    sqlPedido, Statement.RETURN_GENERATED_KEYS)) {

                psPedido.setLong(1, pedido.getUsuarioId());
                psPedido.setString(2, pedido.getStatus().name());
                psPedido.setBigDecimal(3, pedido.getValorTotal());
                psPedido.setTimestamp(4, Timestamp.valueOf(pedido.getDataCriacao()));
                psPedido.executeUpdate();

                long pedidoId;
                try (ResultSet rs = psPedido.getGeneratedKeys()) {
                    rs.next();
                    pedidoId = rs.getLong(1);
                }

                try (PreparedStatement psItem = conn.prepareStatement(sqlItem)) {
                    for (ItemPedido item : pedido.getItens()) {
                        psItem.setLong(1, pedidoId);
                        psItem.setLong(2, item.getProdutoId());
                        psItem.setInt(3, item.getQuantidade());
                        psItem.setBigDecimal(4, item.getPrecoUnitario());
                        psItem.addBatch();
                    }
                    psItem.executeBatch();
                }

                conn.commit();
                return pedidoId;

            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Falha ao salvar pedido", e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Falha de conexão com o banco", e);
        }
    }

    public Optional<Pedido> buscarPorId(Long id) {
        String sqlPedido = "SELECT * FROM pedido WHERE id = ?";
        String sqlItens = "SELECT * FROM item_pedido WHERE pedido_id = ?";

        try (Connection conn = dataSource.getConnection()) {

            Pedido pedido = null;

            try (PreparedStatement psPedido = conn.prepareStatement(sqlPedido)) {
                psPedido.setLong(1, id);
                try (ResultSet rs = psPedido.executeQuery()) {
                    if (rs.next()) {
                        pedido = new Pedido(rs.getLong("usuario_id"), new ArrayList<>());
                        pedido.setId(rs.getLong("id"));
                        pedido.setStatus(StatusPedido.valueOf(rs.getString("status")));
                    }
                }
            }

            if (pedido == null) {
                return Optional.empty();
            }

            try (PreparedStatement psItens = conn.prepareStatement(sqlItens)) {
                psItens.setLong(1, id);
                try (ResultSet rs = psItens.executeQuery()) {
                    while (rs.next()) {
                        ItemPedido item = new ItemPedido(
                                rs.getLong("produto_id"),
                                rs.getInt("quantidade"),
                                rs.getBigDecimal("preco_unitario")
                        );
                        pedido.getItens().add(item);
                    }
                }
            }

            pedido.calcularTotal();
            return Optional.of(pedido);

        } catch (SQLException e) {
            throw new RuntimeException("Falha ao buscar pedido", e);
        }
    }
}