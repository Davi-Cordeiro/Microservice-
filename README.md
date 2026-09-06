<h1 align="center">🛒 Marketplace Distribuído</h1>

<p align="center">
  <strong>Arquitetura de microsserviços com Django + Spring Boot</strong>
</p>

<p align="center">
  Projeto de estudo focado em backend, APIs REST, JDBC, SQL, testes e comunicação entre serviços.
</p>

<p align="center">
  🚧 <strong>Em desenvolvimento</strong>
</p>

---

## 🧩 Arquitetura

```text
                    ┌─────────────────────┐
                    │      Frontend       │
                    │       Django        │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │   Django Service    │
                    │                     │
                    │ • Usuários          │
                    │ • Catálogo          │
                    │ • Carrinho          │
                    │ • Frontend          │
                    └──────────┬──────────┘
                               │
                         REST / JSON
                               │
                               ▼
                    ┌─────────────────────┐
                    │ Spring Boot Service │
                    │                     │
                    │ • Pedidos           │
                    │ • Regras de negócio │
                    │ • JDBC              │
                    │ • Persistência      │
                    └──────────┬──────────┘
                               │
                               ▼
                         🗄️ Banco de Dados
```

Cada serviço possui **responsabilidades e banco de dados próprios**, mantendo o projeto desacoplado.

---

## ⚙️ Tecnologias

### Django

* Python
* Django
* SQLite/MySQL
* HTML/CSS
* REST

### Spring Boot

* Java
* Spring Boot
* Spring Web
* Bean Validation
* JDBC
* SQL
* JUnit
* Mockito
* Maven

---

## 📌 Estado do projeto

### 🟢 Spring Boot

* [x] Estrutura inicial
* [x] Modelagem de `Pedido`
* [x] Modelagem de `ItemPedido`
* [x] Status do pedido
* [x] DTOs
* [x] Mapper
* [x] Validação
* [x] Regra de aprovação
* [x] Repository com JDBC
* [x] Transações
* [x] `POST /api/pedidos`
* [x] `GET /api/pedidos/{id}`
* [x] Tratamento de pedido não encontrado
* [x] Testes automatizados

### 🟡 Django

* [x] Estrutura inicial
* [ ] Cadastro de usuários
* [ ] Catálogo de produtos
* [ ] Carrinho
* [ ] Frontend
* [ ] Integração com Spring Boot
* [ ] Fluxo completo de criação de pedidos

---

## 🛍️ Domínio

### `Pedido`

Representa uma compra realizada pelo usuário.

Possui:

* ID
* Usuário
* Valor total
* Status
* Itens

### `ItemPedido`

Representa um produto dentro de um pedido.

Possui:

* Produto
* Quantidade
* Preço

### `StatusPedido`

```text
APROVADO
RECUSADO
```

---

## 🌐 API

### Criar pedido

```http
POST /api/pedidos
```

Exemplo:

```json
{
  "usuarioId": 1,
  "itens": [
    {
      "produtoId": 10,
      "quantidade": 2,
      "preco": 199.90
    }
  ]
}
```

### Buscar pedido

```http
GET /api/pedidos/{id}
```

---

## 💰 Regra de negócio

O pedido é avaliado pelo valor total:

```text
Total <= R$ 5.000,00 → APROVADO

Total > R$ 5.000,00 → RECUSADO
```

A regra está isolada em um serviço específico:

```text
RegraAprovacaoService
```

Isso mantém a regra de negócio separada da camada de persistência e dos controllers.

---

## 🗄️ Persistência

O serviço Spring Boot utiliza **JDBC puro**, sem JPA/Hibernate.

Principais recursos utilizados:

```text
DataSource
Connection
PreparedStatement
ResultSet
Transactions
Generated Keys
```

A criação de um pedido ocorre dentro de uma transação:

```text
BEGIN
  ↓
Criar Pedido
  ↓
Obter ID gerado
  ↓
Criar Itens
  ↓
COMMIT
```

Em caso de erro:

```text
ROLLBACK
```

---

## 🧪 Testes

O projeto possui testes para diferentes camadas do serviço de pedidos:

* Controller
* Service
* Mapper
* Repository
* Regras de negócio
* Validações

Objetivo:

> Garantir que cada camada funcione de forma independente e que as regras principais do sistema sejam verificadas automaticamente.

---

## 🗺️ Roadmap

### Backend

* [x] Pedido
* [x] Persistência JDBC
* [x] Regras de aprovação
* [x] API REST
* [x] Testes
* [ ] Tratamento global de exceções
* [ ] Integração Django → Spring
* [ ] Testes de integração

### Django

* [ ] Usuários
* [ ] Produtos
* [ ] Carrinho
* [ ] Frontend
* [ ] Comunicação com Spring Boot

### Infraestrutura

* [ ] Docker
* [ ] Docker Compose
* [ ] Configuração por ambiente
* [ ] Documentação da API

---

## 🎯 Objetivos de aprendizado

Este projeto foi criado para praticar conceitos de backend e arquitetura de sistemas, principalmente:

* Java
* Spring Boot
* SQL
* JDBC
* REST APIs
* DTOs e Mappers
* Arquitetura em camadas
* Transações
* Testes automatizados
* Microsserviços
* Comunicação entre aplicações
* Separação de responsabilidades

---

## 🚀 Próximo grande objetivo

Transformar os dois serviços em um fluxo completo:

```text
Usuário
   ↓
Django
   ↓
Catálogo / Carrinho
   ↓
Criação do Pedido
   ↓
Spring Boot
   ↓
Validação + Regra de Negócio
   ↓
Persistência
   ↓
Resposta
```

---

<p align="center">
  <strong>🚧 Projeto em desenvolvimento</strong>
  <br>
  Construído para aprender, experimentar e evoluir.
</p>
