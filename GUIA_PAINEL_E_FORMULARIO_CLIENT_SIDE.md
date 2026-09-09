# Guia — Painel de Produtos e Formulário de Cadastro (Client-Side)

Documento de referência com as decisões e passos discutidos para implementar:
1. Um painel de produtos (listagem), no estilo da home, consumindo a API via JavaScript.
2. Um formulário de cadastro de produto, também consumindo a API via JavaScript.
3. Ajustes de estrutura de pastas e de `settings.py` necessários para suportar isso.

Este documento é só **guia de decisão e passo a passo** — não contém a implementação pronta.

---

## 1. Como o fluxo funciona no Django (contexto)

```
navegador pede uma URL
      ↓
urls.py decide qual "view" atende esse pedido
      ↓
a view busca dados (via o model) e decide o que fazer
      ↓
a view entrega esses dados a um template (.html)
      ↓
o template monta o HTML final devolvido ao navegador
```

Hoje a `home` (`produtos/views.py`) só faz `render(request, 'home.html')` — não busca produto nenhum, por isso a home não mostra catálogo.

No modelo **client-side**, esse fluxo muda de papel: a view Django só entrega um HTML "casca vazia" (a estrutura da página, sem dado nenhum dentro). Depois que o navegador termina de carregar essa casca, entra um **segundo fluxo**, que não passa mais pelo Django-view — é o JavaScript, rodando no navegador do usuário, que faz uma chamada HTTP própria (`fetch`) direto para a API (`/produto/api/`) e só então preenche a página com os dados recebidos. Ou seja, existem dois "pedidos" separados acontecendo em momentos diferentes: um pedido inicial de página (HTML) e, depois, um ou mais pedidos de dados (JSON) feitos pelo próprio navegador.

Isso é diferente do fluxo server-side, onde tudo (busca de dado + montagem do HTML) acontece de uma vez só, do lado do servidor, antes de qualquer coisa chegar ao navegador.

**Por que isso importa na prática:** como a montagem da tela agora depende de uma segunda chamada que acontece *depois* que a página já apareceu, você precisa lidar explicitamente com o tempo entre "página carregou" e "dado chegou" (estado de carregamento) e com a possibilidade de essa segunda chamada falhar (estado de erro) — coisa que no server-side simplesmente não existe, porque se o dado não chegasse, a página inteira já teria falhado antes de ser enviada.

---

## 2. Estrutura de pastas recomendada

Hoje existe uma inconsistência: `produtos/templates/home.html` não está namespaçado pelo nome do app. Funciona porque só existe um app até agora, mas quando surgirem outros apps (`carrinho`, `contas`, conforme o roadmap), arquivos de mesmo nome em apps diferentes podem colidir — o Django, com `APP_DIRS: True`, procura o template em todos os apps instalados e usa o **primeiro** que encontrar com aquele nome, na ordem de `INSTALLED_APPS`; se dois apps tiverem um `form.html`, por exemplo, o app errado pode "vencer" silenciosamente, sem erro nenhum avisando. A convenção do Django para evitar isso é sempre ter uma subpasta com o nome do app dentro de `templates/` e `static/`.

```
Microservice-/
└── ecommerc_django/                  ← raiz do projeto (onde fica manage.py)
    ├── templates/                    ← templates COMPARTILHADOS entre apps
    │   └── base.html                 ← layout comum (menu, rodapé, <head>)
    ├── static/                       ← estáticos COMPARTILHADOS
    │   └── css/
    │       └── base.css
    │
    └── produtos/
        ├── templates/
        │   └── produtos/             ← namespace do app (evita colisão)
        │       ├── home.html
        │       ├── painel.html       ← painel de produtos
        │       └── cadastro.html     ← formulário de cadastro
        └── static/
            └── produtos/             ← namespace do app, de novo
                ├── js/
                │   ├── painel.js
                │   └── cadastro.js
                └── css/
                    └── produtos.css
```

**Por que essa divisão:**
- `templates/produtos/...` → tudo específico do app produtos. O caminho usado no `render()` fica `produtos/painel.html`, `produtos/cadastro.html` (o prefixo é o namespace, não uma pasta redundante).
- `templates/` na raiz → só o que é genuinamente compartilhado (ex: `base.html`). Precisa ser declarado no `settings.py`.
- `static/produtos/js/...` → JS específico do painel e do cadastro. Referenciado no template como `produtos/js/painel.js`.
- `static/` na raiz → CSS/JS genéricos de layout (reset CSS, link do Tailwind via CDN, etc.). Também precisa ser declarado.

**Ajustes necessários no `settings.py` (hoje nenhum dos dois existe):**
- `TEMPLATES[0]['DIRS']` está vazio (`[]`) — precisa apontar para a pasta `templates/` da raiz do projeto (a variável `BASE_DIR`, que já existe no `settings.py`, é o caminho absoluto até essa raiz — é ela que serve de referência, em vez de escrever o caminho na mão). Sem esse ajuste, o `base.html` compartilhado simplesmente não é encontrado, porque `APP_DIRS: True` só olha *dentro* de cada app, nunca na raiz do projeto.
- `STATICFILES_DIRS` **não existe** no arquivo — só existe `STATIC_URL` (que é apenas o prefixo da URL usado pelo navegador, tipo `/static/...`, e não diz ao Django onde procurar os arquivos de fato no disco). Precisa ser criado apontando para a pasta `static/` da raiz, do mesmo jeito que `TEMPLATES.DIRS`.
- Isso vale só para **desenvolvimento**. Em produção, o Django não serve arquivos estáticos diretamente por padrão — existe um comando (`collectstatic`) que reúne tudo (estáticos da raiz + de cada app) numa única pasta de saída, servida depois por outra coisa (nginx, whitenoise, etc.). Não é urgente agora, mas é bom já saber que "funciona local" ≠ "funciona em produção" nesse ponto.

Quando surgirem novos apps (`contas`, `carrinho`), a mesma lógica se repete: cada um ganha seu próprio `templates/<app>/` e `static/<app>/`.

---

## 3. Painel de produtos (listagem via fetch)

### No template (`painel.html`)
- Uma div vazia que serve de container onde os cards serão inseridos (ex: `id="lista-produtos"`). Ela começa vazia de propósito — é o JS quem vai preenchê-la depois.
- Um indicador de "carregando..." visível por padrão (pode ser um texto simples ou um spinner), escondido pelo JS assim que os dados chegarem — sem isso, o usuário vê uma tela em branco por um tempo indefinido e pode achar que travou.
- Um espaço reservado (pode estar oculto por padrão) para a mensagem de erro, caso o `fetch` falhe.
- O `<script>` da página carregado no fim do `<body>` (ou com o atributo `defer` no `<head>`), garantindo que a div já existe no DOM no momento em que o JS tentar acessá-la — um erro comum é colocar o script antes do HTML dela existir, e o JS falhar silenciosamente ao tentar manipular um elemento que ainda não existe.

### No JavaScript (`painel.js`)
1. Esperar o DOM carregar (evento `DOMContentLoaded`, ou simplesmente confiar que o script já está no fim do body).
2. Fazer um `fetch` do tipo **GET** para `/produto/api/` (a `ProductView` atual).
   - GET não precisa de token CSRF — CSRF só importa em requisições que **mudam** dado (POST/PUT/PATCH/DELETE); GET é considerado "seguro" por convenção HTTP.
3. `fetch` por si só não lança erro em respostas HTTP tipo 404/500 — ele só falha (rejeita a Promise) em problema de rede (sem internet, servidor fora do ar, DNS, etc.). Por isso é preciso checar manualmente `response.ok` (ou `response.status`) *antes* de tratar o corpo como sucesso — senão um erro 500 do servidor pode ser processado como se fosse uma lista de produtos vazia, sem avisar o usuário.
4. Converter a resposta para JSON (`response.json()` — que também é assíncrono, precisa de `await` ou `.then()`).
5. Percorrer o array de produtos e montar o HTML de cada card (nome, preço, estoque, categoria):
   - Ao inserir texto vindo da API dentro do HTML, prefira `textContent` a `innerHTML` sempre que o conteúdo for só texto (nome do produto, descrição) — `innerHTML` interpreta o que for inserido como HTML/JS, o que abre espaço para um tipo de falha de segurança (XSS) se algum dia esse campo vier de um usuário mal-intencionado que cadastrou um produto com `<script>` no nome, por exemplo. Reservar `innerHTML` só para a estrutura fixa do card (as tags), nunca para o dado dinâmico em si.
   - Formatar o preço num formato de moeda (ex: `R$ 19,90`) em vez de mostrar o número cru do jeito que a API devolve — o JavaScript tem uma API nativa pra isso (`Intl.NumberFormat`), não precisa montar a formatação na mão.
6. Esconder o "carregando..." só depois que os cards forem efetivamente inseridos no DOM, não antes.
7. Envolver os passos 2 a 5 num `try/catch`: se o `fetch` falhar (rede, DNS, API fora do ar), cair no `catch` e mostrar a mensagem de erro reservada no HTML, escondendo o "carregando...". Sem isso, uma falha de rede deixa a tela travada no "carregando..." para sempre, sem explicação nenhuma pro usuário.

### Detalhe pendente: categoria no serializer
A API hoje devolve `categoria` como **ID numérico**, não o nome — o serializer padrão do DRF (`fields = '__all__'`) representa uma chave estrangeira (`ForeignKey`) apenas pelo valor da chave primária, ele não faz esse "join" sozinho. Para mostrar o nome da categoria no card, existem duas saídas:
- Trocar o campo `categoria` no serializer por algo que já traga o nome (por exemplo, um campo do tipo `StringRelatedField`, que usa o `__str__` do model relacionado — que no caso da `Categoria` já devolve o nome).
- Ou criar um campo aninhado, que devolve um objeto completo da categoria (id + nome) em vez de só o id.
Resolver isso no serializer é bem mais simples do que tentar, do lado do JS, buscar a lista de categorias à parte e cruzar manualmente pelos IDs.

### Ponto de atenção futuro: paginação
O roadmap já prevê paginação na API (`DEFAULT_PAGINATION_CLASS`). Quando isso for ativado, a resposta do `fetch` deixa de ser um array direto e passa a ser um objeto com chaves tipo `results`, `count`, `next`, `previous` — o JS que monta os cards precisaria ler `resposta.results` em vez do array cru. Vale ter isso em mente para não quebrar o painel quando a paginação for ligada depois.

---

## 4. Formulário de cadastro (client-side)

### Peça faltando na API hoje
`ProductView` só sabe **listar** (GET) — não **criar** (POST). Duas formas de resolver:
- Trocar `ListAPIView` por `ListCreateAPIView` (DRF) na mesma view/URL — passa a aceitar GET (lista) e POST (cria) no mesmo endpoint, sem precisar duplicar `queryset`/`serializer_class`. **Recomendado**, menos uma URL para gerenciar.
- Ou criar uma view nova só para criação, com URL própria (faz sentido se, no futuro, cadastro exigir uma regra bem diferente da listagem — ex: permissões distintas).

### No template (`cadastro.html`)
- Um `<form>` com campos batendo com o que o serializer espera: nome, descrição, preço, estoque, categoria.
- O campo `categoria` provavelmente vira um `<select>` — e esse `<select>` precisa ser **populado com dados reais**, não fixo no HTML, porque as categorias existentes mudam com o tempo. Isso implica numa API de categorias que ainda não existe (um serializer + uma view de listagem para o model `Categoria`), e o `cadastro.js` faria um segundo `fetch` (GET) nessa API só para montar as opções do `<select>` antes do usuário poder escolher uma.
- Botão de submit — vale desabilitá-lo enquanto a requisição de criação estiver em andamento, para evitar que o usuário clique duas vezes e crie o mesmo produto duplicado.
- Espaço reservado para mensagens de erro/sucesso, idealmente perto de cada campo (não só uma mensagem genérica no topo).

### No JavaScript (`cadastro.js`)
1. Capturar o evento `submit` do form e chamar `event.preventDefault()` logo no início — sem isso, o navegador tenta recarregar a página e enviar os dados do jeito tradicional (comportamento padrão de HTML puro), o que anula a ideia de ser client-side.
2. Ler os valores dos campos (via `FormData`, que já lê o form inteiro de uma vez, ou input a input) e montar um objeto JS simples com chaves iguais aos nomes dos campos do serializer/model (ex: `product_name`, `description`, `preco`, `estoque`, `categoria`).
3. **CSRF é obrigatório aqui** (diferente do GET do painel): a autenticação de sessão do Django exige esse token em qualquer requisição que muda dado, como proteção contra um tipo de ataque (CSRF) onde outro site tentaria disparar esse POST escondido, usando a sessão logada do usuário sem ele saber. Na prática:
   - O Django já seta um cookie chamado `csrftoken` no navegador, automaticamente, assim que qualquer página do site é carregada (isso já acontece hoje, mesmo sem o formulário existir ainda).
   - O JS precisa ler o valor desse cookie (não tem API pronta simples pra isso — precisa parsear `document.cookie` manualmente, ou usar um trecho utilitário curto e padrão que o próprio Django documenta para esse fim) e mandar esse valor no header da requisição, com o nome exato `X-CSRFToken`.
   - Se esse header vier ausente ou errado, o Django recusa a requisição com status `403 Forbidden`, **antes mesmo** de chegar no serializer — então se aparecer um 403 nos testes, o primeiro lugar a olhar é o token, não a lógica de validação de negócio.
4. `fetch` do tipo **POST** para `/produto/api/`, com o header `Content-Type: application/json`, o header do CSRF, e o corpo sendo o objeto do passo 2 convertido para texto JSON (`JSON.stringify`).
5. Tratar a resposta, olhando o `status`:
   - `201` (criado) → sucesso: mostrar mensagem, limpar os campos do form, e/ou redirecionar o usuário para o painel (ex: `window.location.href`).
   - `400` (erro de validação) → o corpo da resposta vem em JSON, com uma chave por campo que falhou e uma lista de mensagens (esse formato já vem pronto do DRF, não precisa ser inventado) — por exemplo, um preço negativo geraria uma entrada explicando isso especificamente no campo `preco`. Vale mapear essa resposta para exibir a mensagem certa ao lado do campo certo, em vez de um erro genérico tipo "algo deu errado".
   - Falha de rede/timeout (igual ao painel) → cair num `catch`, mostrar mensagem amigável, reabilitar o botão de submit para o usuário poder tentar de novo.

### Pontos a decidir antes de implementar
- **Quem pode cadastrar?** Hoje não há autenticação nenhuma no projeto — qualquer pessoa que ache a URL do formulário consegue cadastrar produto, sem login. Aceitável para uso interno/teste agora, mas é um ponto a resolver (via o item de autenticação do roadmap) antes de expor isso publicamente.
- **Categoria vazia:** se nenhuma categoria existir ainda no banco, o `<select>` fica sem opção nenhuma — seria preciso cadastrar categorias primeiro (hoje só é possível pelo `/admin`, quando o model estiver registrado lá) ou o próprio formulário de cadastro de produto precisaria, de alguma forma, permitir criar uma categoria nova ali mesmo (o que é uma decisão de produto, não só técnica).
- **Validação amigável:** o banco já impede preço/estoque negativo por causa do tipo do campo (`DecimalField`, `PositiveIntegerField`), mas isso sozinho, se chegasse até o banco, geraria um erro técnico feio, não uma mensagem legível. É o serializer (na camada da API) quem já intercepta isso antes e devolve a mensagem amigável em JSON — por isso vale confiar nessa camada de validação, e não tentar reimplementar as mesmas regras manualmente no JS (o JS pode, no máximo, fazer uma validação rápida de campo vazio antes de nem chamar a API, como melhoria de UX, mas a validação de verdade continua sendo responsabilidade do servidor).
- **Fluxo pós-salvamento:** ficar na mesma tela com mensagem de sucesso e permitir cadastrar outro produto em seguida, ou redirecionar direto para o painel já mostrando o produto novo? Isso muda o que o passo 5 (`201`) precisa fazer.

---

## 5. Ordem sugerida para implementar

1. Ajustar `settings.py` (`TEMPLATES.DIRS` e `STATICFILES_DIRS`), apontando para as pastas `templates/` e `static/` da raiz do projeto.
2. Criar a estrutura de pastas descrita na seção 2 (namespace por app).
3. Criar `base.html` compartilhado (menu, `<head>`, rodapé) e fazer os outros templates herdarem dele.
4. Implementar o painel (seção 3) primeiro — é só GET, mais simples de validar isoladamente antes de mexer em criação de dado.
5. Resolver o serializer para expor o nome da categoria (se for mostrar isso no painel) — e, se o `<select>` do formulário for depender de uma API de categorias, criar essa API agora também.
6. Trocar `ListAPIView` por `ListCreateAPIView` na view de produtos.
7. Implementar o formulário de cadastro (seção 4), reaproveitando o `base.html` já criado.
8. Testar manualmente com o DevTools do navegador aberto na aba "Network", conferindo o corpo da requisição/resposta de cada `fetch` (GET do painel, GET de categorias, POST do cadastro) — é a forma mais direta de confirmar que o CSRF, o formato do JSON e os status HTTP estão saindo como esperado antes de considerar cada parte pronta.
9. Revisar autenticação antes de expor isso fora do ambiente local.
