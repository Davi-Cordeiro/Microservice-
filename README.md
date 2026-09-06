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

### 🐍 Django

* ⬜ Estrutura inicial do projeto
* ⬜ App `produtos`
* ⬜ Modelagem do catálogo
* ⬜ Usuários
* ⬜ Produtos
* ⬜ Carrinho
* ⬜ Frontend
* ⬜ Integração com Spring Boot

---

## 📦 Domínio de Pedidos

### `Pedido`

Representa uma compra realizada no marketplace.

Responsável por agrupar os itens e armazenar informações como:

* ID
* Usuário
* Valor total
* Status
* Itens do pedido

### `ItemPedido`

Representa um produto pertencente a um pedido.

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

O serviço Spring Boot disponibiliza endpoints para gerenciamento de pedidos.

### Criar pedido

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

### Buscar pedido

```http
GET /api/pedidos/{id}
```

Retorna os dados do pedido quando encontrado.

Caso o pedido não exista, a API retorna uma resposta de **recurso não encontrado (`404 Not Found`)**.

---

## 💰 Regra de negócio

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

## 🧱 Arquitetura do Spring Boot

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

* ⬜ Modelar produtos
* ⬜ Implementar catálogo
* ⬜ Implementar usuários
* ⬜ Implementar carrinho
* ⬜ Desenvolver frontend
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

* Java
* Spring Boot
* Python
* Django
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

Este projeto faz parte do processo de aprendizado de desenvolvimento backend, com foco em **Java, Spring Boot, SQL, JDBC e arquitetura de microsserviços**.

A ideia é construir o sistema de forma incremental, aplicando na prática conceitos de arquitetura, persistência, APIs REST, testes e comunicação entre serviços.
