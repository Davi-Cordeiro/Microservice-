<h1 align="center">🛒 Marketplace Distribuído</h1>

<p align="center">
  Projeto de estudo de uma aplicação de e-commerce distribuída, construída com Django e Spring Boot.
</p>

<p align="center">
  <b>Django</b> é responsável por catálogo, usuários, carrinho e interface web,
  enquanto <b>Spring Boot</b> processa os pedidos.
</p>

---

## 🧩 Arquitetura

O projeto é dividido em dois serviços independentes, cada um com responsabilidades próprias.

```text
                         ┌──────────────────────┐
                         │        Usuário       │
                         └──────────┬───────────┘
                                    │
                                    ▼
                    ┌─────────────────────────────┐
                    │       Django Service        │
                    │                             │
                    │  • Usuários                 │
                    │  • Catálogo                 │
                    │  • Carrinho                 │
                    │  • Interface Web            │
                    └──────────────┬──────────────┘
                                   │
                              REST / JSON
                                   │
                                   ▼
                    ┌─────────────────────────────┐
                    │    Spring Boot Service      │
                    │                             │
                    │  • Processamento de pedidos │
                    │  • Regras de negócio        │
                    │  • Persistência com JDBC    │
                    └──────────────┬──────────────┘
                                   │
                                   ▼
                              🗄️ Banco de Dados
```

### Serviços

| Serviço       | Tecnologia         | Responsabilidade                             |
| ------------- | ------------------ | -------------------------------------------- |
| 🐍 Django     | Python / Django    | Usuários, catálogo, carrinho e interface web |
| ☕ Spring Boot | Java / Spring Boot | Processamento e persistência de pedidos      |

Os serviços são independentes e se comunicam através de **HTTP utilizando REST e JSON**.

---

## ⚙️ Tecnologias

### 🐍 Django Service

* Python
* Django
* HTML / CSS
* SQLite / MySQL
* REST

### ☕ Spring Boot Service

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

## 📌 Estado atual

> ✅ concluído · 🟨 parcial / em andamento · ⬜ pendente

### 🐍 Django

* ✅ Estrutura inicial do projeto
* ✅ App `produtos`
* 🟨 Modelagem do catálogo (falta preço, estoque e categoria no model `Product`)
* ✅ Endpoint de listagem de produtos (`GET /produto/api/`)
* ⬜ Usuários / autenticação
* ⬜ Carrinho
* 🟨 Frontend (página inicial simples, sem catálogo renderizado)
* ⬜ Integração com Spring Boot

### ☕ Spring Boot

* ✅ Estrutura inicial
* ✅ Modelagem de `Pedido`
* ✅ Modelagem de `ItemPedido`
* ✅ Status do pedido
* ✅ DTOs
* ✅ Mapper
* ✅ Validação
* ✅ Regra de aprovação
* ✅ Repository com JDBC
* ✅ Transações
* ✅ `POST /api/pedidos`
* ✅ `GET /api/pedidos/{id}`
* ✅ Tratamento de pedido não encontrado
* ✅ Testes automatizados

---

## 📦 Domínio

### 🐍 Django — `Product`

Representa um item do catálogo disponível para venda.

Possui atualmente:

* ID
* Nome (`product_name`)
* Descrição
* Situação (`active`)

> Em construção: preço, estoque e categoria ainda serão adicionados ao model — sem eles, o catálogo não consegue alimentar o payload de pedido esperado pelo Spring Boot (`precoUnitario`).

### ☕ Spring Boot — `Pedido`

Representa uma compra realizada no marketplace.

Responsável por agrupar os itens e armazenar informações como:

* ID
* Usuário
* Valor total
* Status
* Itens do pedido

#### `ItemPedido`

Representa um produto pertencente a um pedido.

Possui:

* Produto
* Quantidade
* Preço

#### `StatusPedido`

```text
APROVADO
RECUSADO
```

---

## 🌐 API

### 🐍 Django

```http
GET /produto/api/
```

Lista os produtos com `active=True` do catálogo.

Exemplo de resposta:

```json
[
  {
    "id": 1,
    "product_name": "Camiseta Básica",
    "description": "Camiseta 100% algodão",
    "active": true
  }
]
```

> Endpoints ainda não implementados: autenticação (`/contas/...`), carrinho (`/carrinho/...`) e checkout (`/pedidos/finalizar/`) — este último é quem vai chamar a API do Spring Boot abaixo.

### ☕ Spring Boot

O serviço Spring Boot disponibiliza endpoints para gerenciamento de pedidos.

#### Criar pedido

```http
POST /api/pedidos
```

Exemplo de requisição:

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

#### Buscar pedido

```http
GET /api/pedidos/{id}
```

Retorna os dados do pedido quando encontrado.

Caso o pedido não exista, a API retorna uma resposta de **recurso não encontrado (`404 Not Found`)**.

---

## 💰 Regra de negócio (Spring Boot)

O pedido passa por uma regra de aprovação baseada no valor total:

```text
Total <= R$ 5.000,00
        ↓
    APROVADO

Total > R$ 5.000,00
        ↓
    RECUSADO
```

A regra está isolada no:

```text
RegraAprovacaoService
```

Dessa forma, a regra de negócio não fica acoplada ao controller ou à camada de persistência.

---

## 🧱 Arquitetura em camadas

### 🐍 Django

```text
URLConf
    ↓
View
    ↓
Serializer
    ↓
Model (ORM)
    ↓
Database
```

Hoje só existe a camada de leitura (`ProductView` → `ProductSerializer` → `Product`). Views de autenticação, carrinho e checkout ainda serão adicionadas seguindo essa mesma estrutura.

### ☕ Spring Boot

O serviço segue uma arquitetura em camadas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Também são utilizados:

```text
DTOs
Mapper
Domain Models
Exceptions
Validation
```

### Fluxo de criação

```text
HTTP Request
     ↓
Controller
     ↓
Validation
     ↓
Service
     ↓
Regra de aprovação
     ↓
Repository
     ↓
Database
     ↓
HTTP Response
```

---

## 🗄️ Persistência

### 🐍 Django

A persistência do Django utiliza o **ORM padrão do framework** — os models em `models.py` geram o SQL automaticamente através de migrations (`makemigrations` / `migrate`), sem necessidade de escrever queries manualmente. Banco atual: SQLite (`db.sqlite3`), local.

### ☕ Spring Boot

A persistência do Spring Boot utiliza **JDBC puro**, sem JPA ou Hibernate.

Principais recursos utilizados:

* `DataSource`
* `Connection`
* `PreparedStatement`
* `ResultSet`
* Generated Keys
* Transactions

A criação de um pedido e seus itens ocorre dentro de uma única transação:

```text
BEGIN
  ↓
Inserir Pedido
  ↓
Obter ID gerado
  ↓
Inserir Itens
  ↓
COMMIT
```

Em caso de erro:

```text
ROLLBACK
```

Isso garante que o pedido não seja parcialmente persistido.

---

## 🧪 Testes

### 🐍 Django

Ainda não há testes automatizados (`tests.py` está vazio). É prioridade no roadmap escrever testes de model e serializer com `TestCase` / `APITestCase` antes de avançar para carrinho e checkout.

### ☕ Spring Boot

O módulo de pedidos possui testes automatizados para diferentes responsabilidades:

* Controller
* Service
* Mapper
* Repository
* Regras de negócio
* Validações

O objetivo é verificar o comportamento das principais camadas antes da integração entre os serviços.

---

## 🗺️ Roadmap

### 🐍 Django

* 🟨 Modelar produtos (falta preço, estoque e categoria)
* ⬜ Escrever testes automatizados
* ⬜ Implementar usuários / autenticação
* ⬜ Implementar carrinho
* ⬜ Desenvolver frontend (renderizar catálogo, não só a home)
* ⬜ Criar fluxo de checkout
* ⬜ Integrar com Spring Boot

### ☕ Spring Boot

* ⬜ Melhorias na API
* ⬜ Tratamento global de exceções
* ⬜ Testes de integração

### 🔗 Microsserviços

* ⬜ Comunicação Django → Spring Boot
* ⬜ Fluxo completo de checkout
* ⬜ Tratamento de falhas entre serviços
* ⬜ Docker
* ⬜ Docker Compose
* ⬜ Configuração por ambiente

---

## 🎯 Objetivos de aprendizado

Este projeto foi criado para praticar conceitos de desenvolvimento backend e arquitetura distribuída:

* Python
* Django
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

## 🚀 Fluxo esperado

Quando o projeto estiver completo, o fluxo principal será:

```text
Usuário
   ↓
Django
   ↓
Catálogo / Carrinho
   ↓
Checkout
   ↓
Spring Boot
   ↓
Validação
   ↓
Regra de negócio
   ↓
Persistência
   ↓
Resposta
```

---

## 📚 Sobre o projeto

Este projeto faz parte do processo de aprendizado de desenvolvimento backend, com foco em **Python, Django e DRF** de um lado, e **Java, Spring Boot, SQL e JDBC** do outro — unidos por uma arquitetura de microsserviços.

A ideia é construir os dois serviços de forma incremental, aplicando na prática conceitos de arquitetura, persistência, APIs REST, testes e comunicação entre serviços.
