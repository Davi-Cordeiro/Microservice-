<h1 align="center">🛒 Marketplace Distribuído</h1>

<p align="center">
  Projeto de estudo de uma aplicação de e-commerce distribuída em dois serviços independentes.
</p>

<p align="center">
  <b>Django</b> para catálogo, usuários, carrinho e frontend
  <br>
  <b>Spring Boot</b> para processamento e persistência de pedidos
</p>

<hr>

<h2>📌 Visão Geral</h2>

<p>
Este projeto consiste em uma aplicação de e-commerce dividida em dois serviços,
desenvolvidos com tecnologias diferentes e responsáveis por domínios distintos.
</p>

<ul>
  <li><b>Django:</b> catálogo, usuários, carrinho e interface web.</li>
  <li><b>Spring Boot:</b> processamento, regras de negócio e persistência dos pedidos.</li>
</ul>

<p>
A comunicação entre os serviços será realizada através de uma
<b>API REST utilizando JSON sobre HTTP</b>.
</p>

<blockquote>
  <b>Status atual:</b> o serviço Spring Boot já possui o fluxo básico de criação
  e persistência de pedidos implementado. O serviço Django ainda está em fase
  inicial de estruturação.
</blockquote>

<hr>

<h2>🏗️ Arquitetura</h2>

<pre>
                         ┌─────────────────────┐
                         │     Navegador       │
                         │       Usuário       │
                         └──────────┬──────────┘
                                    │
                                    │ HTTP
                                    ▼
              ┌────────────────────────────────────┐
              │           SERVIÇO A                │
              │             Django                 │
              │                                    │
              │  • Catálogo                        │
              │  • Usuários                        │
              │  • Carrinho                        │
              │  • Frontend                        │
              │  • Integração com pedidos          │
              │                                    │
              │  Banco próprio                     │
              └────────────────┬───────────────────┘
                               │
                               │ HTTP / JSON
                               │
                               │ POST /api/pedidos
                               ▼
              ┌────────────────────────────────────┐
              │           SERVIÇO B                │
              │          Spring Boot               │
              │                                    │
              │  • REST Controller                 │
              │  • Regras de negócio               │
              │  • Domínio de pedidos              │
              │  • JDBC puro                       │
              │                                    │
              │  Banco próprio                     │
              └────────────────────────────────────┘
</pre>

<p>
Cada serviço possui responsabilidade sobre seus próprios dados.
O Spring Boot não acessa diretamente as tabelas do Django e o Django
não acessa diretamente o banco do Spring Boot.
</p>

<hr>

<h2>📊 Estado Atual do Projeto</h2>

<h3>🐍 Django</h3>

<p>
O projeto Django já possui a estrutura inicial criada, incluindo:
</p>

<ul>
  <li>Projeto Django</li>
  <li><code>manage.py</code></li>
  <li>Configuração inicial do projeto</li>
  <li>Aplicação <code>produtos</code></li>
  <li>Configuração inicial do banco SQLite</li>
  <li>Django Admin e componentes padrão</li>
</ul>

<p>
Atualmente, a aplicação <code>produtos</code> ainda está em desenvolvimento.
Os models, views, catálogo, carrinho, frontend e integração com o Spring Boot
ainda serão implementados.
</p>

<h4>Estrutura atual</h4>

<pre>
ecommerc_django/
├── manage.py
│
├── ecommerc_django/
│   ├── __init__.py
│   ├── settings.py
│   ├── urls.py
│   ├── asgi.py
│   └── wsgi.py
│
└── produtos/
    ├── __init__.py
    ├── admin.py
    ├── apps.py
    ├── migrations/
    ├── models.py
    ├── tests.py
    └── views.py
</pre>

<h4>Próximas etapas do Django</h4>

<ul>
  <li>Implementar <code>Categoria</code></li>
  <li>Implementar <code>Produto</code></li>
  <li>Criar migrations</li>
  <li>Implementar catálogo</li>
  <li>Implementar autenticação</li>
  <li>Criar carrinho</li>
  <li>Desenvolver frontend</li>
  <li>Implementar checkout</li>
  <li>Criar cliente HTTP para o Spring Boot</li>
  <li>Integrar o fluxo completo de compra</li>
</ul>

<hr>

<h3>☕ Spring Boot</h3>

<p>
O serviço Spring Boot é atualmente a parte mais desenvolvida do projeto.
</p>

<p>
O backend foi desenvolvido utilizando:
</p>

<ul>
  <li><b>Java</b></li>
  <li><b>Spring Boot</b></li>
  <li><b>JDBC puro</b></li>
  <li><b>SQL</b></li>
  <li><b>Bean Validation</b></li>
</ul>

<p>
O projeto não utiliza JPA/Hibernate, permitindo trabalhar diretamente
com JDBC e compreender melhor o processo de persistência e transações.
</p>

<h4>Estrutura</h4>

<pre>
pedidos/
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/ecommerce/pedidos/
│   │   │       ├── PedidosApplication.java
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   └── PedidoController.java
│   │   │       │
│   │   │       ├── model/
│   │   │       │   ├── Pedido.java
│   │   │       │   ├── ItemPedido.java
│   │   │       │   └── StatusPedido.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── ItemRequestDTO.java
│   │   │       │   ├── PedidoRequestDTO.java
│   │   │       │   ├── PedidoResponseDTO.java
│   │   │       │   └── PedidoMapper.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   └── PedidoRepository.java
│   │   │       │
│   │   │       └── service/
│   │   │           ├── PedidoService.java
│   │   │           └── RegraAprovacaoService.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── schema.sql
│   │
│   └── test/
│       └── java/
│           └── com/ecommerce/pedidos/
│               └── PedidosApplicationTests.java
</pre>

<hr>

<h2>📦 Domínio</h2>

<h3>Pedido</h3>

<p>Um pedido possui:</p>

<ul>
  <li>ID</li>
  <li>ID do usuário</li>
  <li>Status</li>
  <li>Valor total</li>
  <li>Data de criação</li>
  <li>Lista de itens</li>
</ul>

<pre>
Pedido
├── id
├── usuarioId
├── status
├── valorTotal
├── dataCriacao
└── itens
</pre>

<p>
O valor total é calculado a partir dos subtotais dos itens do pedido.
</p>

<h3>ItemPedido</h3>

<p>Cada item possui:</p>

<ul>
  <li>ID do produto</li>
  <li>Quantidade</li>
  <li>Preço unitário</li>
</ul>

<p>
O subtotal é calculado através de:
</p>

<pre>subtotal = preço unitário × quantidade</pre>

<h3>StatusPedido</h3>

<p>Atualmente existem dois estados:</p>

<pre>
APROVADO
RECUSADO
</pre>

<hr>

<h2>📨 DTOs</h2>

<p>
A API utiliza DTOs para separar os dados recebidos pela API dos objetos
internos de domínio.
</p>

<h3>PedidoRequestDTO</h3>

<pre>
{
  "usuarioId": 1,
  "itens": [
    {
      "produtoId": 5,
      "quantidade": 2,
      "precoUnitario": 49.90
    }
  ]
}
</pre>

<p>Os dados recebidos possuem validações utilizando Bean Validation:</p>

<ul>
  <li><code>usuarioId</code> não pode ser nulo</li>
  <li>A lista de itens não pode estar vazia</li>
  <li><code>produtoId</code> não pode ser nulo</li>
  <li>Quantidade deve ser positiva</li>
  <li>Preço deve ser positivo</li>
</ul>

<h3>PedidoResponseDTO</h3>

<pre>
{
  "pedidoId": 42,
  "status": "APROVADO",
  "valorTotal": 99.80
}
</pre>

<hr>

<h2>🔄 Mapper</h2>

<p>
O <code>PedidoMapper</code> é responsável pela conversão entre os objetos
da API e os objetos de domínio.
</p>

<pre>
PedidoRequestDTO
        ↓
      Pedido
        ↓
PedidoResponseDTO
</pre>

<p>
Essa separação evita que os DTOs da API sejam utilizados diretamente como
objetos de domínio.
</p>

<hr>

<h2>💳 Regra de Aprovação</h2>

<p>
O serviço possui uma regra simples para simular uma análise de pagamento.
</p>

<pre>
Valor ≤ R$ 5.000,00
        ↓
    APROVADO

Valor > R$ 5.000,00
        ↓
    RECUSADO
</pre>

<p>
A regra está isolada na classe <code>RegraAprovacaoService</code>, permitindo
que sua implementação seja alterada futuramente sem misturar a lógica de
negócio com a persistência ou o Controller.
</p>

<hr>

<h2>🌐 API</h2>

<h3>POST /api/pedidos</h3>

<p><b>Objetivo:</b> criar um novo pedido.</p>

<h4>Request</h4>

<pre>
{
  "usuarioId": 1,
  "itens": [
    {
      "produtoId": 5,
      "quantidade": 2,
      "precoUnitario": 49.90
    },
    {
      "produtoId": 8,
      "quantidade": 1,
      "precoUnitario": 19.90
    }
  ]
}
</pre>

<h4>Response</h4>

<pre>
{
  "pedidoId": 42,
  "status": "APROVADO",
  "valorTotal": 119.70
}
</pre>

<h4>Fluxo</h4>

<pre>
HTTP Request
     ↓
PedidoController
     ↓
PedidoService
     ↓
PedidoMapper
     ↓
Calcula total
     ↓
RegraAprovacaoService
     ↓
PedidoRepository
     ↓
Banco de dados
     ↓
PedidoResponseDTO
     ↓
HTTP Response
</pre>

<hr>

<h2>🗄️ Persistência</h2>

<p>
A persistência do serviço Spring Boot utiliza <b>JDBC puro</b>, sem
JPA ou Hibernate.
</p>

<p>São utilizados diretamente:</p>

<ul>
  <li><code>DataSource</code></li>
  <li><code>Connection</code></li>
  <li><code>PreparedStatement</code></li>
  <li><code>ResultSet</code></li>
  <li><code>Statement.RETURN_GENERATED_KEYS</code></li>
</ul>

<p>
A criação do pedido utiliza uma transação para garantir que o pedido e
seus respectivos itens sejam persistidos juntos.
</p>

<pre>
BEGIN TRANSACTION
        ↓
INSERT pedido
        ↓
Obtém ID gerado
        ↓
INSERT itens
        ↓
COMMIT
</pre>

<p>
Caso ocorra uma falha durante a operação:
</p>

<pre>ROLLBACK</pre>

<hr>

<h2>💰 Valores Monetários</h2>

<p>
Os valores financeiros são representados através de
<code>BigDecimal</code>.
</p>

<p>
A escolha evita problemas de precisão associados ao uso de
<code>double</code> e <code>float</code> em operações monetárias.
</p>

<hr>

<h2>🗃️ Banco de Dados</h2>

<h3>pedido</h3>

<pre>
pedido
├── id
├── usuario_id
├── status
├── valor_total
└── data_criacao
</pre>

<h3>item_pedido</h3>

<pre>
item_pedido
├── id
├── pedido_id
├── produto_id
├── quantidade
└── preco_unitario
</pre>

<p>Relacionamento:</p>

<pre>
pedido
   │
   │ 1:N
   ▼
item_pedido
</pre>

<p>
O banco do Spring armazena apenas os IDs de usuário e produto recebidos
pela API. Não existem relacionamentos diretos com as tabelas pertencentes
ao serviço Django.
</p>

<hr>

<h2>🔗 Integração Django → Spring Boot</h2>

<p>
A integração planejada seguirá o seguinte fluxo:
</p>

<pre>
Usuário
   ↓
Django
   ↓
Carrinho
   ↓
Finalizar compra
   ↓
POST /api/pedidos
   ↓
Spring Boot
   ↓
Processamento do pedido
   ↓
Banco
   ↓
Resposta JSON
   ↓
Django
   ↓
Frontend
</pre>

<p>
O Django será responsável por montar a requisição a partir dos produtos
presentes no carrinho.
</p>

<p>
O Spring Boot será responsável pelo processamento e persistência do pedido.
</p>

<hr>

<h2>🧪 Testes</h2>

<h3>Spring Boot</h3>

<p>
O projeto possui a estrutura inicial de testes. A cobertura será ampliada
conforme as funcionalidades forem desenvolvidas.
</p>

<p>Os próximos testes devem cobrir:</p>

<ul>
  <li>Cálculo do total</li>
  <li>Subtotal dos itens</li>
  <li>Regra de aprovação</li>
  <li>Regra de recusa</li>
  <li>Validação dos DTOs</li>
  <li><code>PedidoService</code></li>
  <li>Persistência</li>
  <li>Endpoint REST</li>
</ul>

<h3>Django</h3>

<p>
Os testes serão implementados conforme as funcionalidades do serviço
forem desenvolvidas.
</p>

<h3>Integração</h3>

<p>
Após a implementação dos dois serviços, será testado o fluxo completo:
</p>

<pre>
Django
  ↓
POST /api/pedidos
  ↓
Spring Boot
  ↓
Banco
  ↓
Response
  ↓
Django
</pre>

<hr>

<h2>🔐 Segurança</h2>

<p>
A segurança ainda não está completamente implementada.
</p>

<p>Como evolução futura, estão previstas:</p>

<ul>
  <li>Variáveis de ambiente para configurações sensíveis</li>
  <li>Proteção da comunicação entre os serviços</li>
  <li>Autenticação entre Django e Spring Boot</li>
  <li>Configuração adequada de CORS</li>
  <li>Configuração de produção do Django</li>
  <li>Remoção de secrets do código-fonte</li>
</ul>

<hr>

<h2>🚧 Próximas Etapas</h2>

<h3>Django</h3>

<ul>
  <li>⬜ Criar model <code>Categoria</code></li>
  <li>⬜ Criar model <code>Produto</code></li>
  <li>⬜ Criar migrations</li>
  <li>⬜ Implementar catálogo</li>
  <li>⬜ Implementar autenticação</li>
  <li>⬜ Criar carrinho</li>
  <li>⬜ Desenvolver frontend</li>
  <li>⬜ Implementar checkout</li>
  <li>⬜ Criar integração HTTP com Spring Boot</li>
</ul>

<h3>Spring Boot</h3>

<ul>
  <li>✅ Criar projeto Spring Boot</li>
  <li>✅ Criar domínio <code>Pedido</code></li>
  <li>✅ Criar domínio <code>ItemPedido</code></li>
  <li>✅ Criar <code>StatusPedido</code></li>
  <li>✅ Criar DTOs</li>
  <li>✅ Criar Mapper</li>
  <li>✅ Implementar cálculo do total</li>
  <li>✅ Implementar regra de aprovação</li>
  <li>✅ Implementar Repository com JDBC</li>
  <li>✅ Implementar transação de persistência</li>
  <li>✅ Implementar <code>POST /api/pedidos</code></li>
  <li>✅ Implementar validação dos dados recebidos</li>
  <li>⬜ Implementar <code>GET /api/pedidos/{id}</code></li>
  <li>⬜ Melhorar tratamento global de exceções</li>
  <li>⬜ Aumentar cobertura de testes</li>
</ul>

<h3>Integração</h3>

<ul>
  <li>⬜ Definir contrato final entre os serviços</li>
  <li>⬜ Implementar chamada Django → Spring</li>
  <li>⬜ Testar comunicação entre os serviços</li>
  <li>⬜ Testar fluxo completo de compra</li>
  <li>⬜ Implementar autenticação entre serviços</li>
</ul>

<hr>

<h2>🌱 Possíveis Expansões</h2>

<p>
Depois que o fluxo principal estiver funcionando, o projeto poderá evoluir
com funcionalidades como:
</p>

<ul>
  <li>🐳 Docker</li>
  <li>🐳 Docker Compose</li>
  <li>🐘 PostgreSQL</li>
  <li>🔑 JWT</li>
  <li>🔐 Autenticação entre serviços</li>
  <li>📖 Swagger / OpenAPI</li>
  <li>🐇 RabbitMQ</li>
  <li>⚡ Processamento assíncrono</li>
  <li>🔔 Sistema de notificações</li>
  <li>📋 Histórico detalhado de pedidos</li>
  <li>📊 Painel administrativo</li>
  <li>🚀 Deploy</li>
  <li>📈 Observabilidade e logs estruturados</li>
</ul>

<p>
Essas funcionalidades são consideradas <b>extras</b> e não fazem parte
do núcleo atual do projeto.
</p>

<hr>

<h2>🎯 Objetivos de Aprendizado</h2>

<h3>Java / Spring Boot</h3>

<ul>
  <li>Programação Orientada a Objetos</li>
  <li>Arquitetura em camadas</li>
  <li>REST APIs</li>
  <li>Spring Boot</li>
  <li>Bean Validation</li>
  <li>DTOs</li>
  <li>Mappers</li>
  <li>Regras de negócio</li>
  <li>JDBC</li>
  <li>SQL</li>
  <li>Transações</li>
  <li>Persistência</li>
  <li>Integração entre serviços</li>
</ul>

<h3>Python / Django</h3>

<ul>
  <li>Django</li>
  <li>Models</li>
  <li>Views</li>
  <li>Autenticação</li>
  <li>ORM</li>
  <li>Desenvolvimento web</li>
  <li>JavaScript</li>
  <li>Integração HTTP</li>
  <li>Consumo de APIs</li>
</ul>

<h3>Arquitetura e Engenharia</h3>

<ul>
  <li>Arquitetura distribuída</li>
  <li>Comunicação entre serviços</li>
  <li>Contratos de API</li>
  <li>Separação de responsabilidades</li>
  <li>Bancos de dados independentes</li>
  <li>Git e desenvolvimento colaborativo</li>
</ul>

<hr>

<h2>📌 Resumo</h2>

<p>
O projeto consiste em um marketplace dividido em dois serviços:
</p>

<pre>
┌─────────────────────┐
│       Django        │
│                     │
│ Catálogo            │
│ Usuários            │
│ Carrinho            │
│ Frontend            │
└──────────┬──────────┘
           │
           │ REST / JSON
           ▼
┌─────────────────────┐
│    Spring Boot      │
│                     │
│ Pedidos             │
│ Regras de negócio   │
│ JDBC                │
│ Persistência        │
└─────────────────────┘
</pre>

<p>
Atualmente, o <b>Spring Boot possui o fluxo básico de criação e persistência
de pedidos implementado</b>, enquanto o <b>Django está na fase inicial de
construção da aplicação</b>.
</p>

<p>
O objetivo final é criar um fluxo completo de compra no qual o usuário
navega pelo catálogo Django, adiciona produtos ao carrinho, finaliza a
compra e o pedido é processado pelo microsserviço Spring Boot.
</p>

<hr>

<p align="center">
  <b>🚀 Projeto em desenvolvimento</b>
  <br>
  Java • Spring Boot • JDBC • SQL • Django • REST
</p>
