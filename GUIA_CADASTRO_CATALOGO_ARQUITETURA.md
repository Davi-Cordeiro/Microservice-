# Guia — Cadastro e Catálogo: Django Form/View vs. API + JavaScript

Este documento detalha a decisão em aberto: **como o formulário de cadastro de
produto e a página de catálogo vão gravar/ler dados** — via Django puro
(sem JavaScript) ou via a API que já existe no projeto (com JavaScript).

Não contém implementação pronta — é material de decisão e referência.
Complementa o `GUIA_PAINEL_E_FORMULARIO_CLIENT_SIDE.md` (que já detalha só
o caminho client-side); aqui os dois caminhos são explicados lado a lado.

---

## 1. O ponto central: uma página HTML nunca grava no banco sozinha

Por mais que pareça que "o HTML manda o dado pro banco", isso nunca acontece
de verdade — todo formulário, em qualquer site, sempre passa por um código
do lado do servidor no meio do caminho. A pergunta certa não é "o HTML vai
pro banco?", e sim: **qual código do servidor recebe esse envio, e como ele
grava?**

No Django, existem duas respostas possíveis pra essa pergunta, e elas levam
a arquiteturas bem diferentes:

```
Navegador                    Servidor (Django)                Banco
   |                                |                            |
   |--- POST com os dados --------->|                            |
   |                                |-- ModelForm valida -------->|
   |                                |-- form.save() ------------->| (grava)
   |<-- HTML novo (redirect) -------|                            |

                      OU

   |--- fetch() POST em JSON ------>|                            |
   |                                |-- Serializer (DRF) valida ->|
   |                                |-- serializer.save() ------->| (grava)
   |<-- JSON de resposta -----------|                            |
   |
   | (o JS no navegador é quem decide o que fazer com esse JSON)
```

Repare: nos dois casos, no fim das contas é o **Django ORM** quem grava no
banco (`form.save()` e `serializer.save()` fazem, por baixo dos panos, a
mesma coisa: `Product.objects.create(...)`). A diferença real está em **quem
fala com quem**, e **o que volta pro navegador**.

---

## 2. Opção A — Django Form + View tradicional (zero JavaScript)

### Como funciona

Um `ModelForm` é uma classe que "espelha" o model `Product`: ela já sabe
quais campos existem, que tipo cada um é, e gera automaticamente:
- os campos HTML (`<input>`, `<select>`, etc.) correspondentes;
- as regras de validação (preço não pode ser negativo, campo obrigatório
  não pode vir vazio, etc.) — as mesmas regras que já estão definidas no
  `models.py`, sem você reescrever nada.

Uma única view lida com os dois métodos HTTP possíveis nessa URL:
- **GET** → o usuário está só abrindo a página. A view cria o form vazio e
  manda pro template.
- **POST** → o usuário enviou os dados preenchidos. A view recria o form
  agora populado com o que veio (`request.POST`), pede pra ele se validar
  (`form.is_valid()`), e:
  - se for válido, chama `form.save()` (grava no banco) e redireciona o
    usuário para outra página (o painel, por exemplo);
  - se não for válido, devolve o mesmo template, com o form preenchendo
    de novo os campos com o que o usuário digitou e mostrando as mensagens
    de erro ao lado de cada campo — tudo isso pronto, o Django já faz
    sozinho.

O template usa uma tag de template (`{{ form }}` ou renderizando campo a
campo) — o HTML dos inputs não é escrito por você, é gerado pelo próprio
`ModelForm` a partir da definição do model.

### Catálogo, no mesmo estilo

A view busca os produtos direto: `Product.objects.filter(active=True)` —
e entrega essa lista de objetos Python pro template. O template usa um
laço (`{% for produto in produtos %}`) pra desenhar um card por produto,
acessando os atributos do objeto diretamente (`produto.product_name`,
`produto.preco`). Quando o navegador pede essa página, **ela já chega com
os produtos dentro do HTML** — não existe uma segunda etapa de busca.

### Vantagens
- **Nenhuma linha de JavaScript.** Todo o fluxo (validar, salvar, mostrar
  erro, listar) é responsabilidade do Django, testável com as ferramentas
  que você já usa (`manage.py test`).
  Sem `fetch`, sem CSRF manual, sem tratar JSON de erro na mão.
- Menos peças móveis: uma view, um form/queryset, um template. Não tem
  "duas fontes da verdade" (uma view Django tradicional e um endpoint de
  API fazendo praticamente a mesma coisa).
- Erros de validação aparecem automaticamente ao lado de cada campo, sem
  você escrever esse mapeamento manualmente.

### Desvantagens
- A API REST que você já construiu (`ListCreateAPIView`) fica **sem uso**
  nessas duas telas — ela segue existindo e funcionando, mas só seria útil
  se outro cliente (app mobile, o Spring Boot, um frontend separado)
  precisasse consumir os produtos.
- Toda navegação recarrega a página inteira (comportamento padrão de HTML)
  — não tem a sensação de "aplicativo" fluido que uma SPA/fetch dá.
- Se um dia você quiser um frontend totalmente separado do Django (ex: um
  app React puro), esse trabalho de tela não seria reaproveitado — teria
  que ser refeito consumindo a API.

---

## 3. Opção B — HTML + fetch consumindo a API (client-side)

Esse caminho já está detalhado a fundo no
`GUIA_PAINEL_E_FORMULARIO_CLIENT_SIDE.md` — resumo rápido aqui, só pra
comparação lado a lado:

- O template HTML só tem a estrutura (form vazio, ou container vazio pro
  catálogo) — nenhum dado already embutido nele.
- Um JavaScript à parte faz `fetch` GET (catálogo) ou `fetch` POST
  (cadastro) contra `/produto/api/`, que já é `ListCreateAPIView`.
- Quem valida os dados no POST é o **serializer do DRF**, não um
  `ModelForm` — são mecanismos parecidos, mas duas implementações
  diferentes (o `ModelForm` não entra em jogo nesse caminho).
- CSRF precisa ser tratado manualmente no JS (ler o cookie, mandar no
  header `X-CSRFToken`).
- Erros de validação chegam como JSON (`{"preco": ["..."]}"`) e o próprio
  JS decide onde/como mostrar isso na tela.

### Vantagens
- Reaproveita a API que já existe — uma única fonte de verdade para
  "como validar e gravar um produto", usada tanto pela tela quanto por
  qualquer outro cliente futuro.
- Mais alinhado com a arquitetura de microserviço que o projeto já propõe
  (Django expõe API, algo consome).
- Navegação sem recarregar a página inteira.

### Desvantagens
- Exige escrever e manter JavaScript (captura de submit, fetch, CSRF,
  parse de erro, estados de loading/erro) — mais código, mais lugar pra
  ter bug.
- Validação "duplicada" em espírito: o serializer do DRF já valida os
  dados, mas ele não é o mesmo código do `ModelForm` — se um dia você
  também quiser um form Django tradicional em outra tela, a regra de
  validação teria que ser replicada (ou centralizada de outro jeito).

---

## 4. Lado a lado

| | Opção A — Django Form/View | Opção B — API + fetch |
|---|---|---|
| JavaScript necessário | Nenhum | Sim (fetch, CSRF, parse de erro) |
| Quem valida | `ModelForm` (usa o model) | Serializer DRF |
| Quem grava no banco | `form.save()` | `serializer.save()` (via API) |
| Recarrega a página? | Sim, a cada envio | Não |
| Reaproveita a API existente? | Não | Sim |
| Curva de aprendizado extra | Baixa (só Django) | Mais alta (fetch, CSRF, JSON) |
| Bom se o frontend crescer separado do Django | Não (teria que refazer) | Sim (a API já está pronta) |

---

## 5. Como decidir

Perguntas pra te ajudar a bater o martelo:

1. **Você quer aprender/praticar JavaScript agora, ou prefere adiar isso?**
   Se quiser adiar → Opção A.
2. **Alguma outra coisa (app mobile, outro frontend, o próprio Spring Boot)
   vai precisar ler/criar produtos pela API no futuro próximo?**
   Se sim → vale a API já nascer testada em uso real → Opção B.
3. **O objetivo agora é ver o CRUD de produto funcionando o quanto antes,
   com o menor número de partes móveis?**
   Se sim → Opção A é mais direta.
4. **Você prefere que cadastro e catálogo sigam o mesmo padrão do resto do
   projeto** (que hoje já expõe a API pro Spring Boot eventualmente
   consumir)? → Opção B mantém tudo consistente com essa ideia.

**Não recomendo misturar** — ou as duas telas (cadastro e catálogo) seguem
a Opção A, ou as duas seguem a Opção B. Ter uma tela em cada estilo
funciona, mas deixa o projeto com duas formas diferentes de fazer a mesma
coisa (criar/listar produto), o que confunde quem for mexer depois.

### É possível migrar depois?

Sim, e sem jogar nada fora. O `models.py` (a definição do `Product`) não
muda em nenhuma das duas opções — é a mesma tabela, os mesmos campos. Só a
camada de apresentação (view + template, ou view + JS) muda. Começar pela
Opção A e migrar pra Opção B mais tarde é perfeitamente viável, dá só um
pouco mais de trabalho de reescrever a tela, não o banco.

---

## 6. Checklist de implementação por opção

**Opção A (Django Form/View):**
1. Criar `produtos/forms.py` com um `ModelForm` para `Product`.
2. Criar a view de cadastro, tratando GET e POST na mesma função/classe.
3. Criar a view de catálogo, com o queryset + passagem pro template.
4. Templates: `cadastro.html` (renderiza o `form`), `painel.html` (laço
   sobre a lista de produtos).
5. Rotas novas em `produtos/urls.py` apontando pras duas views.
6. Nenhum ajuste na API é necessário — ela continua existindo em paralelo,
   sem ser usada por essas duas telas.

**Opção B (API + fetch):** já detalhado passo a passo no
`GUIA_PAINEL_E_FORMULARIO_CLIENT_SIDE.md`, seções 3 e 4.

---

## 7. Decisão tomada neste projeto (registrado em 2026-09-10)

Ficou decidido: **cadastro vai usar JavaScript** (fetch) no frontend. O
backend está sendo construído primeiro.

Importante: o que está sendo construído é uma **variação híbrida**, não a
Opção B "pura" descrita na seção 3:

- Em vez de usar o **serializer do DRF** (`ProductSerializer`, o mesmo que
  a `ListCreateAPIView` já usa em `/produto/api/`), o cadastro está sendo
  feito com um **`ModelForm` do Django puro** (`ProductForm`), chamado de
  dentro de uma **`View` comum do Django** (não uma view do DRF).
- O POST dessa view devolve **JSON na mão** (`JsonResponse`), montado
  manualmente (`{'valid': True}` / `{'valid': False, 'errors': ...}`) —
  não é o formato de erro que o DRF gera sozinho (que viria como
  `{"campo": ["mensagem"]}` diretamente, sem o envelope `valid`/`errors`).

**Ponto em aberto que vale decidir antes de seguir:** isso cria **duas
rotas diferentes capazes de criar um produto** — a `ListCreateAPIView` em
`/produto/api/` (POST) e essa view nova. Duas opções:
- **Consolidar**: apagar essa `ProductForm`/view nova e usar só o POST que
  já existe em `/produto/api/` (o `fetch` do cadastro chamaria essa URL,
  igual o painel já chama pro GET). Menos código duplicado.
- **Manter separado**: se a ideia é ter uma rota própria de "cadastro via
  formulário" diferente da API pública (por exemplo, se no futuro a API
  `/produto/api/` for exigir autenticação de outro tipo, ou for pensada só
  pra consumo externo/Spring Boot), faz sentido manter os dois. Nesse caso,
  o formato de JSON de erro dessa view custom deveria, idealmente, ficar
  parecido com o do DRF, só pra quem for escrever o JS não ter que tratar
  dois formatos de erro diferentes dependendo da tela.

Este guia não decide isso por você — só documenta que a decisão existe e
precisa ser tomada conscientemente, não por acidente.

---

## 8. Erros encontrados durante a revisão (histórico, pra não repetir)

Lista dos bugs reais encontrados enquanto o `ProductForm`/view de cadastro
foi sendo escrita, com a explicação do porquê — útil como checklist de
"coisas que já erraram uma vez aqui":

1. **Duas classes `ProductForm` no mesmo arquivo** — a segunda substituía
   a primeira silenciosamente (Python não avisa). A primeira, morta,
   listava campos que não existem no model (`name`, `price`, `image` —
   o model só tem `product_name`, `description`, `active`), o que geraria
   `FieldError` na importação **se** fosse a versão usada.
2. **Lógica de validação dentro do `Meta`** — um `if form.is_valid(): ...`
   foi colocado dentro da classe `Meta` (que é só configuração:
   `model`/`fields`/`exclude`). Isso roda uma vez, na importação do
   arquivo, não a cada requisição — e nesse momento não existe nenhum
   request HTTP real ainda. Validar/salvar só faz sentido dentro de uma
   *view*, executada quando uma requisição chega.
3. **`from django.http import request`** — importava um **módulo** do
   Django (não uma requisição de verdade). `request.POST` nesse módulo
   não existe. O `request` de verdade é o parâmetro que o Django passa
   pra função/método da view a cada chamada — não algo que se importa.
4. **`views.View` sem o import correspondente** — faltava
   `from django.views import View` (ou `from django import views`). Sem
   isso, `NameError` na hora de definir a classe (quebra o site inteiro,
   igual os bugs de import anteriores no projeto).
5. **Métodos nomeados `form_get`/`form_post`** — o Django só chama
   automaticamente métodos chamados exatamente `get`, `post`, `put`,
   `delete`, etc. Nomes diferentes nunca são executados; uma requisição
   GET cairia no comportamento padrão (405 - método não permitido).
6. **`form.save()` esquecido** — o form validava (`is_valid()` retornava
   `True`) mas nunca gravava nada no banco. Faltava chamar `form.save()`
   dentro do `if form.is_valid():`, antes de responder sucesso.
7. **Import morto remanescente** — depois de corrigir o item 3, sobrou
   `request` ainda importado de `django.http` sem nenhum uso no arquivo.
   Não quebra nada, mas é lixo a limpar.
8. **Nome de classe confuso** — `ValidadeProductForm` lê como "data de
   validade do produto", não como "view que valida o formulário do
   produto". Sugestão: renomear pra algo como `ProductFormView` ou
   `CadastroProdutoView`, seguindo o padrão de nomes já usado
   (`ProductView`).

---

## 9. Roadmap para terminar o cadastro sozinho (sem precisar checar comigo)

Assumindo a decisão da seção 7 (JS no frontend, backend com
`ModelForm` + `View` custom devolvendo JSON — ajuste os passos se decidir
consolidar com a API do DRF):

1. **Limpar `views.py`**:
   - Remover o import morto (`request` de `django.http`).
   - Opcional: renomear a view (item 8 da seção 8).
2. **Adicionar a rota em `produtos/urls.py`**: um `path(...)` novo
   apontando pra essa view, com um `name=` (ex: `"cadastro_produto"`).
3. **Criar o template** `produtos/templates/product_form.html` (ou mover
   pra `produtos/templates/produtos/product_form.html`, namespaçado, se
   for seguir o padrão de pastas do outro guia):
   - Um `<form>` com um input por campo do `ProductForm`
     (`product_name`, `description`) — os `name` dos `<input>` no HTML
     precisam bater com os nomes dos campos do model/form.
   - **Não precisa** de `{% csrf_token %}` do jeito tradicional, porque o
     envio vai ser via `fetch`/JS, não um submit HTML puro — o token vem
     do cookie `csrftoken`, lido manualmente no JS (como já documentado no
     `GUIA_PAINEL_E_FORMULARIO_CLIENT_SIDE.md`, seção 4).
   - Um botão de submit, e um espaço reservado pra mensagens de erro.
4. **Escrever o JS** (por sua conta, como já combinado):
   - Capturar o `submit` do form, `preventDefault()`.
   - Montar o objeto com os campos e mandar via `fetch` POST pra rota do
     passo 2, com o header `X-CSRFToken`.
   - Tratar a resposta: se `valid: true` → sucesso (redirecionar pro
     painel, ou limpar o form); se `valid: false` → percorrer
     `errors` (um dicionário campo → lista de mensagens) e mostrar cada
     mensagem perto do campo correspondente.
5. **Resolver o ponto em aberto da seção 7** (consolidar com a API do DRF
   ou manter separado) — o quanto antes, pra não esquecer e deixar as duas
   rotas divergindo com o tempo.
6. **Testar manualmente** (checklist da seção 10 abaixo).
7. Depois do cadastro funcionando: seguir pro **catálogo/painel**,
   reaproveitando a `ListCreateAPIView` (`/produto/api/`) que já existe —
   passo a passo já documentado no `GUIA_PAINEL_E_FORMULARIO_CLIENT_SIDE.md`,
   seção 3.

---

## 10. Checklist de "pronto" (teste manual, sem precisar de mim pra validar)

- [ ] Abrir a URL do cadastro no navegador → aparece o form vazio (GET
      funcionando, template encontrado).
- [ ] Rodar `python manage.py check` → nenhum erro (garante que nenhum
      import/nome quebrado voltou).
- [ ] Enviar o form vazio → a resposta é `valid: false` com um erro por
      campo obrigatório faltando.
- [ ] Enviar o form preenchido corretamente → a resposta é `valid: true`,
      **e** o produto aparece de verdade no banco (confirmar pelo
      `/admin`, se estiver registrado, ou pelo shell do Django:
      `Product.objects.all()`).
- [ ] Enviar sem o header `X-CSRFToken` (de propósito, pra testar) → devia
      voltar `403 Forbidden` — confirma que a proteção CSRF está mesmo
      ativa nessa rota.
- [ ] Conferir que **não sobrou nenhuma segunda rota concorrente** criando
      produto de outro jeito (a decisão da seção 7 foi tomada e aplicada,
      não só pensada).
