package com.ecommerce.pedidos.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ecommerce.pedidos.model.ItemPedido;
import com.ecommerce.pedidos.model.Pedido;
import com.ecommerce.pedidos.model.StatusPedido;

class PedidoRepositoryTest {

    private DataSource dataSource;
    private PedidoRepository pedidoRepository;

    @BeforeEach
    void setUp() throws SQLException {
        JdbcDataSource h2 = new JdbcDataSource();
        h2.setURL("jdbc:h2:mem:pedidos_test_" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        h2.setUser("sa");
        h2.setPassword("");

        this.dataSource = h2;
        this.pedidoRepository = new PedidoRepository(dataSource);

        criarTabelas();
    }

    private void criarTabelas() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                    CREATE TABLE pedido (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        usuario_id BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL,
                        valor_total NUMERIC(12,2) NOT NULL,
                        data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            stmt.execute("""
                    CREATE TABLE item_pedido (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        pedido_id BIGINT NOT NULL,
                        produto_id BIGINT NOT NULL,
                        quantidade INT NOT NULL,
                        preco_unitario NUMERIC(12,2) NOT NULL,
                        FOREIGN KEY (pedido_id) REFERENCES pedido(id)
                    )
                    """);
        }
    }

    @Test
    void deveSalvarPedidoERetornarIdGerado() {
        ItemPedido item = new ItemPedido(1L, 2, new BigDecimal("49.90"));
        Pedido pedido = new Pedido(1L, List.of(item));
        pedido.calcularTotal();
        pedido.setStatus(StatusPedido.APROVADO);

        Long id = pedidoRepository.salvar(pedido);

        assertNotNull(id);
    }

    @Test
    void deveSalvarERecuperarPedidoComTodosOsItens() {
        ItemPedido item1 = new ItemPedido(1L, 2, new BigDecimal("49.90"));
        ItemPedido item2 = new ItemPedido(2L, 1, new BigDecimal("19.90"));
        Pedido pedidoOriginal = new Pedido(1L, List.of(item1, item2));
        pedidoOriginal.calcularTotal();
        pedidoOriginal.setStatus(StatusPedido.APROVADO);

        Long id = pedidoRepository.salvar(pedidoOriginal);

        Optional<Pedido> resultado = pedidoRepository.buscarPorId(id);

        assertTrue(resultado.isPresent());
        Pedido pedidoRecuperado = resultado.get();
        assertEquals(StatusPedido.APROVADO, pedidoRecuperado.getStatus());
        assertEquals(2, pedidoRecuperado.getItens().size());
        assertEquals(0, new BigDecimal("119.70").compareTo(pedidoRecuperado.getValorTotal()));
    }

    @Test
    void deveRetornarOptionalVazioParaIdInexistente() {
        Optional<Pedido> resultado = pedidoRepository.buscarPorId(999999L);

        assertTrue(resultado.isEmpty());
    }
}