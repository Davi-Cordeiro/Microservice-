# Documentação do Projeto — Marketplace Distribuído (Django + Spring Boot)

## Sumário

1. Visão Geral
2. Arquitetura do Sistema
3. Parte Django — Detalhamento Completo
4. Parte Spring Boot — Detalhamento Completo
5. Contrato de API entre os Serviços
6. Banco de Dados (Modelagem Detalhada)
7. Fluxo Completo de uma Compra
8. Segurança e Autenticação
9. Tratamento de Erros
10. Testes
11. Cronograma Sugerido
12. Organização em Git
13. Dicas Gerais e Boas Práticas
14. Ideias de Expansão (Extras)

---

## 1. Visão Geral

O projeto é um **marketplace simples** dividido em dois serviços independentes que se comunicam via **API REST (JSON sobre HTTP)**:

- **Serviço A — Catálogo e Frontend** (Django + JS + Tailwind): responsável pela experiência do usuário, catálogo de produtos, carrinho e autenticação.
- **Serviço B — Pedidos e Pagamentos** (Spring Boot + JDBC): responsável por processar pedidos, calcular valores, simular pagamento e manter o histórico.

Essa separação simula uma **arquitetura de microsserviços real**, onde cada equipe é dona de uma parte do sistema, com seu próprio banco de dados, e a comunicação acontece exclusivamente por API — nunca acessando o banco um do outro diretamente.

### Por que essa divisão faz sentido
- Você pratica Django puro (models, views, templates ou API), além de JS/Tailwind no front.
- Seu amigo pratica POO aplicada (classes de domínio), JDBC (acesso a banco sem ORM) e Spring Boot (camada REST).
- Ambos praticam **integração entre sistemas heterogêneos**, um dos temas mais cobrados em entrevistas técnicas.

---

## 2. Arquitetura do Sistema

```
┌─────────────────────┐
│   Navegador (User)  │
└─────────┬────────────┘
          │ HTTP (HTML/JS)
          ▼
┌───────────────────────────────────────┐
│         SERVIÇO A — DJANGO             │
│  - Views/Templates ou API              │
│  - Autenticação de usuário             │
│  - Catálogo de produtos                │
│  - Carrinho de compras                 │
│  - Banco: SQLite ou PostgreSQL         │
└─────────┬───────────────────────────────┘
          │ POST /api/pedidos (JSON)
          ▼
┌───────────────────────────────────────┐
│       SERVIÇO B — SPRING BOOT          │
│  - REST Controller                     │
│  - Regras de negócio (POO)             │
│  - Acesso a dados via JDBC             │
│  - Banco: MySQL ou PostgreSQL          │
└─────────────────────────────────────────┘
```

### Por que dois bancos separados?
Isso reforça o conceito de que cada serviço é **dono dos seus próprios dados** (padrão em microsserviços). O Django nunca lê diretamente o banco do Spring, e vice-versa — tudo passa pela API.

---

## 3. Parte Django — Detalhamento Completo

### 3.1 Estrutura de pastas sugerida

```
marketplace_django/
├── manage.py
├── marketplace/          # configurações do projeto
│   ├── settings.py
│   ├── urls.py
├── produtos/             # app de catálogo
│   ├── models.py
│   ├── views.py
│   ├── serializers.py
│   ├── urls.py
├── contas/               # app de autenticação
│   ├── models.py
│   ├── views.py
├── carrinho/             # app do carrinho
│   ├── models.py
│   ├── views.py
├── pedidos/              # app que integra com Spring Boot
│   ├── services.py       # lógica de chamada HTTP
│   ├── views.py
├── static/
│   ├── css/ (tailwind)
│   ├── js/
├── templates/
```

### 3.2 Modelos (models.py)

```python
# produtos/models.py
from django.db import models

class Categoria(models.Model):
    nome = models.CharField(max_length=100)

    def __str__(self):
        return self.nome


class Produto(models.Model):
    nome = models.CharField(max_length=200)
    descricao = models.TextField(blank=True)
    preco = models.DecimalField(max_digits=10, decimal_places=2)
    estoque = models.PositiveIntegerField(default=0)
    categoria = models.ForeignKey(Categoria, on_delete=models.SET_NULL, null=True)
    imagem = models.ImageField(upload_to='produtos/', blank=True, null=True)
    criado_em = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return self.nome
```

```python
# carrinho/models.py
from django.db import models
from django.contrib.auth.models import User
from produtos.models import Produto

class ItemCarrinho(models.Model):
    usuario = models.ForeignKey(User, on_delete=models.CASCADE)
    produto = models.ForeignKey(Produto, on_delete=models.CASCADE)
    quantidade = models.PositiveIntegerField(default=1)
    adicionado_em = models.DateTimeField(auto_now_add=True)

    class Meta:
        unique_together = ('usuario', 'produto')
```

### 3.3 Views principais (exemplo simplificado com Django REST Framework)

```python
# produtos/views.py
from rest_framework import viewsets
from .models import Produto
from .serializers import ProdutoSerializer

class ProdutoViewSet(viewsets.ReadOnlyModelViewSet):
    queryset = Produto.objects.all()
    serializer_class = ProdutoSerializer
```

```python
# pedidos/services.py — Serviço que fala com o Spring Boot
import requests
from django.conf import settings

SPRING_BOOT_URL = settings.SPRING_BOOT_URL  # ex: "http://localhost:8080"

def criar_pedido(usuario_id, itens):
    payload = {
        "usuarioId": usuario_id,
        "itens": [
            {
                "produtoId": item.produto.id,
                "quantidade": item.quantidade,
                "precoUnitario": float(item.produto.preco)
            }
            for item in itens
        ]
    }

    resposta = requests.post(
        f"{SPRING_BOOT_URL}/api/pedidos",
        json=payload,
        timeout=5
    )
    resposta.raise_for_status()
    return resposta.json()
```

```python
# pedidos/views.py
from django.http import JsonResponse
from carrinho.models import ItemCarrinho
from .services import criar_pedido

def finalizar_compra(request):
    itens = ItemCarrinho.objects.filter(usuario=request.user)

    if not itens.exists():
        return JsonResponse({"erro": "Carrinho vazio"}, status=400)

    try:
        resultado = criar_pedido(request.user.id, itens)
    except Exception as e:
        return JsonResponse({"erro": "Falha ao processar pedido", "detalhe": str(e)}, status=502)

    itens.delete()  # limpa carrinho após sucesso
    return JsonResponse(resultado)
```

### 3.4 Frontend (JS + Tailwind)

**Dicas de implementação:**
- Use `fetch()` para adicionar/remover itens do carrinho sem recarregar a página.
- Mostre um "spinner" de carregamento ao clicar em "Finalizar Compra" (o Spring Boot pode demorar alguns milissegundos a segundos para responder).
- Trate erros de rede visualmente — ex: se o Spring Boot estiver fora do ar, mostre uma mensagem amigável, não uma tela quebrada.

```javascript
// static/js/checkout.js
async function finalizarCompra() {
    const btn = document.getElementById('btn-checkout');
    btn.disabled = true;
    btn.innerText = 'Processando...';

    try {
        const resposta = await fetch('/pedidos/finalizar/', {
            method: 'POST',
            headers: { 'X-CSRFToken': getCookie('csrftoken') }
        });

        if (!resposta.ok) throw new Error('Erro ao finalizar compra');

        const dados = await resposta.json();
        alert(`Pedido #${dados.pedidoId} — Status: ${dados.status}`);
    } catch (erro) {
        alert('Não foi possível processar seu pedido agora. Tente novamente.');
    } finally {
        btn.disabled = false;
        btn.innerText = 'Finalizar Compra';
    }
}
```

---

## 4. Parte Spring Boot — Detalhamento Completo

### 4.1 Estrutura de pastas sugerida

```
marketplace-pedidos/
├── src/main/java/com/marketplace/pedidos/
│   ├── PedidosApplication.java
│   ├── controller/
│   │   └── PedidoController.java
│   ├── model/
│   │   ├── Pedido.java
│   │   ├── ItemPedido.java
│   │   └── StatusPedido.java (enum)
│   ├── repository/
│   │   └── PedidoRepository.java   # usando JDBC puro
│   ├── service/
│   │   └── PedidoService.java
│   ├── dto/
│   │   ├── PedidoRequestDTO.java
│   │   └── PedidoResponseDTO.java
│   └── config/
│       └── DataSourceConfig.java
├── src/main/resources/
│   ├── application.properties
│   └── schema.sql
```

### 4.2 Classes de domínio (POO)

```java
// model/StatusPedido.java
public enum StatusPedido {
    PENDENTE, APROVADO, RECUSADO
}
```

```java
// model/ItemPedido.java
public class ItemPedido {
    private Long produtoId;
    private int quantidade;
    private double precoUnitario;

    // construtor, getters e setters

    public double getSubtotal() {
        return quantidade * precoUnitario;
    }
}
```

```java
// model/Pedido.java
import java.time.LocalDateTime;
import java.util.List;

public class Pedido {
    private Long id;
    private Long usuarioId;
    private List<ItemPedido> itens;
    private double valorTotal;
    private StatusPedido status;
    private LocalDateTime criadoEm;

    public double calcularTotal() {
        return itens.stream()
                .mapToDouble(ItemPedido::getSubtotal)
                .sum();
    }

    // construtor, getters e setters
}
```

### 4.3 Acesso a dados com JDBC puro

```java
// repository/PedidoRepository.java
import java.sql.*;

public class PedidoRepository {

    private final DataSource dataSource;

    public PedidoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long salvar(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedido (usuario_id, valor_total, status, criado_em) VALUES (?, ?, ?, NOW())";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, pedido.getUsuarioId());
            stmt.setDouble(2, pedido.getValorTotal());
            stmt.setString(3, pedido.getStatus().name());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Falha ao obter ID gerado");
        }
    }

    public Pedido buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM pedido WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Pedido pedido = new Pedido();
                pedido.setId(rs.getLong("id"));
                pedido.setUsuarioId(rs.getLong("usuario_id"));
                pedido.setValorTotal(rs.getDouble("valor_total"));
                pedido.setStatus(StatusPedido.valueOf(rs.getString("status")));
                return pedido;
            }
            return null;
        }
    }
}
```

### 4.4 Controller REST

```java
// controller/PedidoController.java
import org.springframework.web.bind.annotation.*;
import com.marketplace.pedidos.dto.*;
import com.marketplace.pedidos.service.PedidoService;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public PedidoResponseDTO criarPedido(@RequestBody PedidoRequestDTO request) {
        return pedidoService.processarPedido(request);
    }

    @GetMapping("/{id}")
    public PedidoResponseDTO buscarPedido(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }
}
```

### 4.5 Regra de negócio (simulação de pagamento)

```java
// service/PedidoService.java
public class PedidoService {

    private final PedidoRepository repository;

    public PedidoService(PedidoRepository repository) {
        this.repository = repository;
    }

    public PedidoResponseDTO processarPedido(PedidoRequestDTO request) {
        Pedido pedido = converterParaPedido(request);
        pedido.setValorTotal(pedido.calcularTotal());

        // Regra simples de simulação de pagamento
        StatusPedido status = simularPagamento(pedido.getValorTotal());
        pedido.setStatus(status);

        try {
            Long id = repository.salvar(pedido);
            pedido.setId(id);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao salvar pedido", e);
        }

        return new PedidoResponseDTO(pedido.getId(), pedido.getStatus(), pedido.getValorTotal());
    }

    private StatusPedido simularPagamento(double valor) {
        // Regra fictícia: valores acima de 1000 são recusados (simula limite de crédito)
        return valor <= 1000 ? StatusPedido.APROVADO : StatusPedido.RECUSADO;
    }
}
```

---

## 5. Contrato de API entre os Serviços

**Combinem isso ANTES de codar** — é o ponto mais importante para não ter retrabalho.

### Requisição: Django → Spring Boot

`POST /api/pedidos`

```json
{
  "usuarioId": 1,
  "itens": [
    { "produtoId": 5, "quantidade": 2, "precoUnitario": 49.90 },
    { "produtoId": 8, "quantidade": 1, "precoUnitario": 19.90 }
  ]
}
```

### Resposta: Spring Boot → Django

```json
{
  "pedidoId": 42,
  "status": "APROVADO",
  "valorTotal": 119.70
}
```

### Consulta de status

`GET /api/pedidos/{id}`

```json
{
  "pedidoId": 42,
  "status": "APROVADO",
  "valorTotal": 119.70
}
```

**Dica:** documentem esse contrato num arquivo `API_CONTRACT.md` compartilhado no repositório, ou usem uma ferramenta como **Swagger/OpenAPI** no Spring Boot para gerar documentação automática.

---

## 6. Banco de Dados (Modelagem Detalhada)

### Banco do Django (SQLite ou PostgreSQL)

```
categoria
├── id (PK)
└── nome

produto
├── id (PK)
├── nome
├── descricao
├── preco
├── estoque
├── categoria_id (FK → categoria)
└── criado_em

auth_user (padrão do Django)
├── id (PK)
├── username
├── email
└── password (hash)

item_carrinho
├── id (PK)
├── usuario_id (FK → auth_user)
├── produto_id (FK → produto)
└── quantidade
```

### Banco do Spring Boot (MySQL ou PostgreSQL)

```sql
-- schema.sql
CREATE TABLE pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em DATETIME NOT NULL
);

CREATE TABLE item_pedido (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (pedido_id) REFERENCES pedido(id)
);
```

**Observação:** o `usuario_id` e `produto_id` no banco do Spring são apenas referências numéricas — o Spring Boot **não** tem acesso direto às tabelas de usuário/produto do Django. Ele só recebe os dados já prontos via API.

---

## 7. Fluxo Completo de uma Compra

1. Usuário navega pelo catálogo (Django) e adiciona produtos ao carrinho.
2. Ao clicar em "Finalizar Compra", o JS do frontend chama `/pedidos/finalizar/` (Django).
3. Django monta o payload com os itens do carrinho e faz um `POST` para o Spring Boot.
4. Spring Boot recebe, calcula o total, simula o pagamento, salva no banco via JDBC.
5. Spring Boot responde com `pedidoId`, `status` e `valorTotal`.
6. Django recebe a resposta, limpa o carrinho do usuário e repassa o resultado para o frontend.
7. Frontend mostra a confirmação (ou erro) para o usuário.

---

## 8. Segurança e Autenticação

Para deixar mais realista (e ótimo para portfólio), considerem:

- **Comunicação entre serviços protegida por token**: o Django envia um header `Authorization: Bearer <token>` fixo (ou JWT simples) e o Spring Boot valida antes de processar.
- **CORS**: se o frontend chamar o Spring Boot diretamente (não recomendado neste design, mas possível), configurem CORS no Spring Boot.
- **Nunca exponham o segredo/token no código do frontend (JS)** — a chamada ao Spring Boot deve ser feita pelo backend Django, não pelo navegador do usuário.

---

## 9. Tratamento de Erros

| Cenário | O que fazer |
|---|---|
| Spring Boot fora do ar | Django captura exceção de conexão e mostra mensagem amigável ("Não foi possível processar seu pedido agora") |
| Timeout na chamada | Definir timeout curto (ex: 5s) na requisição e tratar como falha |
| Payload inválido | Spring Boot retorna `400 Bad Request` com mensagem clara |
| Pedido recusado (simulação) | Retornar `200 OK` com `status: RECUSADO` (não é erro técnico, é uma resposta de negócio válida) |
| Erro interno no Spring Boot | Retornar `500` com corpo JSON padronizado, ex: `{"erro": "Erro interno"}` |

---

## 10. Testes

### Lado Django
- Testes unitários com `pytest` ou `unittest` para models e views.
- Mock da chamada HTTP ao Spring Boot (usando `unittest.mock` ou `responses`) para testar sem depender do serviço estar no ar.

### Lado Spring Boot
- Testes unitários com **JUnit** para a lógica de `PedidoService` (ex: testar a regra de aprovação/recusa).
- Testes de integração usando **Postman/Insomnia** para validar os endpoints manualmente antes de conectar com o Django.

### Testando a integração completa
1. Subam os dois serviços localmente (`python manage.py runserver` na porta 8000, Spring Boot na porta 8080).
2. Testem o fluxo completo manualmente pelo navegador.
3. Depois, se quiserem ir além, escrevam um teste end-to-end simples (ex: com `requests` em Python ou um script separado) que simula o fluxo inteiro.

---

## 11. Cronograma Sugerido (5 semanas)

| Semana | Django (você) | Spring Boot (seu amigo) |
|---|---|---|
| 1 | Setup do projeto, models de produto/categoria, autenticação básica | Setup do projeto, conexão JDBC, schema do banco |
| 2 | CRUD de produtos, carrinho de compras | Classes de domínio (POO), repository com JDBC |
| 3 | Frontend com Tailwind, JS para carrinho dinâmico | Controller REST, regra de simulação de pagamento |
| 4 | Integração: chamar API do Spring Boot, tratar respostas | Testar recebimento de pedidos, ajustar contrato de API |
| 5 | Testes, tratamento de erros, polimento visual | Testes, tratamento de erros, documentação da API |

---

## 12. Organização em Git

Sugestão de estrutura de repositórios:

**Opção 1 — Dois repositórios separados** (mais realista para microsserviços)
```
marketplace-django/
marketplace-pedidos-spring/
```

**Opção 2 — Monorepo** (mais simples de gerenciar a dois)
```
marketplace-projeto/
├── django-app/
├── spring-app/
└── README.md (documentação geral, como este arquivo)
```

**Dicas de fluxo de trabalho:**
- Usem branches por funcionalidade (`feature/carrinho`, `feature/pedido-service`).
- Façam um `README.md` em cada projeto explicando como rodar localmente (dependências, comandos, variáveis de ambiente).
- Combinem previamente as portas usadas (ex: Django na 8000, Spring Boot na 8080) para evitar conflito ao rodar os dois ao mesmo tempo.

---

## 13. Dicas Gerais e Boas Práticas

- **Definam o contrato de API por escrito antes de codar** — evita retrabalho e discussões no meio do projeto.
- **Usem variáveis de ambiente** para URLs e portas (ex: `SPRING_BOOT_URL` no Django), nunca hardcode.
- **Loguem as chamadas entre os serviços** (prints ou logs simples) para facilitar debug quando algo não funcionar.
- **Comecem simples**: façam o fluxo básico funcionar ponta a ponta primeiro (mesmo sem estilização), depois refinem.
- **Usem Postman/Insomnia** para testar cada endpoint isoladamente antes de integrar os dois sistemas.
- Se travarem na integração, testem com `curl` direto no terminal para isolar se o problema é no client (Django) ou no server (Spring Boot).

```bash
# Exemplo de teste manual com curl
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{"usuarioId": 1, "itens": [{"produtoId": 5, "quantidade": 2, "precoUnitario": 49.90}]}'
```

---

## 14. Ideias de Expansão (Extras, se sobrar tempo)

- **Avaliações de produtos** (comentários e notas) no Django.
- **Painel administrativo** no Spring Boot para o "lojista" ver todos os pedidos.
- **Fila assíncrona**: em vez de chamada HTTP síncrona, usar uma fila (RabbitMQ) entre os dois serviços — mais avançado, mas ótimo para aprender mensageria.
- **Autenticação real entre serviços** com JWT.
- **Deploy**: Django no Render/Railway, Spring Boot também no Render ou num servidor separado, simulando ambiente de produção real.
- **Notificação por e-mail** ao usuário quando o pedido for aprovado (Django pode fazer isso após receber a resposta do Spring Boot).

---

### Resumo final

Esse projeto cobre, de forma bem equilibrada:

- **Você**: Django (models, views, API), autenticação, JS assíncrono, Tailwind, consumo de API externa.
- **Seu amigo**: POO aplicada, JDBC puro (sem ORM), Spring Boot REST, regras de negócio.
- **Os dois**: integração entre sistemas, contrato de API, tratamento de erros entre serviços, versionamento em Git.

É um projeto de porte médio, mas totalmente viável em ~5 semanas trabalhando em paralelo, e fica muito bem em portfólio por simular uma arquitetura real de mercado.
