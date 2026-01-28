# Votação

## Objetivo

No cooperativismo, cada associado possui um voto e as decisões são tomadas em assembleias, por votação. Imagine que você deve criar uma solução we para gerenciar e participar dessas sessões de votação.
Essa solução deve ser executada na nuvem e promover as seguintes funcionalidades através de uma API REST / Front:

- Cadastrar uma nova pauta
- Abrir uma sessão de votação em uma pauta (a sessão de votação deve ficar aberta por
  um tempo determinado na chamada de abertura ou 1 minuto por default)
- Receber votos dos associados em pautas (os votos são apenas 'Sim'/'Não'. Cada associado
  é identificado por um id único e pode votar apenas uma vez por pauta)
- Contabilizar os votos e dar o resultado da votação na pauta

Para fins de exercício, a segurança das interfaces pode ser abstraída e qualquer chamada para as interfaces pode ser considerada como autorizada. A solução deve ser construída em java com Spring-boot e Angular/React conforme orientação, mas os frameworks e bibliotecas são de livre escolha (desde que não infrinja direitos de uso).

É importante que as pautas e os votos sejam persistidos e que não sejam perdidos com o restart da aplicação.

## Como proceder

Por favor, realize o FORK desse repositório e implemente sua solução no FORK em seu repositório GItHub, ao final, notifique da conclusão para que possamos analisar o código implementado.

Lembre de deixar todas as orientações necessárias para executar o seu código.

### Tarefas bônus

- Tarefa Bônus 1 - Integração com sistemas externos
  - Criar uma Facade/Client Fake que retorna aleátoriamente se um CPF recebido é válido ou não.
  - Caso o CPF seja inválido, a API retornará o HTTP Status 404 (Not found). Você pode usar geradores de CPF para gerar CPFs válidos
  - Caso o CPF seja válido, a API retornará se o usuário pode (ABLE_TO_VOTE) ou não pode (UNABLE_TO_VOTE) executar a operação. Essa operação retorna resultados aleatórios, portanto um mesmo CPF pode funcionar em um teste e não funcionar no outro.

```
// CPF Ok para votar
{
    "status": "ABLE_TO_VOTE
}
// CPF Nao Ok para votar - retornar 404 no client tb
{
    "status": "UNABLE_TO_VOTE
}
```

Exemplos de retorno do serviço

### Tarefa Bônus 2 - Performance

- Imagine que sua aplicação possa ser usada em cenários que existam centenas de
  milhares de votos. Ela deve se comportar de maneira performática nesses
  cenários
- Testes de performance são uma boa maneira de garantir e observar como sua
  aplicação se comporta

### Tarefa Bônus 3 - Versionamento da API

○ Como você versionaria a API da sua aplicação? Que estratégia usar?

## O que será analisado

- Simplicidade no design da solução (evitar over engineering)
- Organização do código
- Arquitetura do projeto
- Boas práticas de programação (manutenibilidade, legibilidade etc)
- Possíveis bugs
- Tratamento de erros e exceções
- Explicação breve do porquê das escolhas tomadas durante o desenvolvimento da solução
- Uso de testes automatizados e ferramentas de qualidade
- Limpeza do código
- Documentação do código e da API
- Logs da aplicação
- Mensagens e organização dos commits
- Testes
- Layout responsivo

## Dicas

- Teste bem sua solução, evite bugs

  Observações importantes
- Não inicie o teste sem sanar todas as dúvidas
- Iremos executar a aplicação para testá-la, cuide com qualquer dependência externa e
  deixe claro caso haja instruções especiais para execução do mesmo
  Classificação da informação: Uso Interno



# desafio-votacao

## Implementação

Este repositório contém uma solução fullstack:
- backend: Spring Boot 3 (Java 17), banco H2 em arquivo, API REST versionada em /api/v1
- frontend: React + Vite

### Backend

Execução:
```
cd backend
mvn spring-boot:run
```

Documentação da API:
- Swagger UI: http://localhost:8080/swagger-ui
- OpenAPI JSON: http://localhost:8080/api-docs

Persistência:
- Banco H2 em arquivo salvo em `./backend/data/votacao`
- Dados persistem entre reinícios

Principais endpoints:
- POST /api/v1/pautas
- GET /api/v1/pautas
- GET /api/v1/pautas/{id}
- POST /api/v1/pautas/{id}/sessoes
- POST /api/v1/pautas/{id}/votos
- GET /api/v1/pautas/{id}/resultado

Cliente CPF (bônus):
- Cliente fake retorna validade e capacidade de voto de forma aleatória
- Se o CPF for inválido ou UNABLE_TO_VOTE, a API retorna 404
- Ainda não contém validação de dígitos de CPF

### Frontend

Execução:
```
cd frontend
npm install
npm run dev
```

Env opcional:
- VITE_API_URL=http://localhost:8080/api/v1

### Docker

Build e execução com Docker Compose:
```
docker compose up -d --build
```

Aplicações:
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui

Para desligar:
```
docker compose down
```

### Testes

```
cd backend
mvn test
```

### Teste de performance (bônus 2)

Executa um grande volume de inserções de votos para observar o comportamento com muitos votos.

```
cd backend
mvn -Dperf=true -Dperf.votes=20000 test -Dtest=VotoPerformanceTest
```

### Observações

- A duração da sessão é 1 minuto por padrão
- Um associado pode votar apenas uma vez por pauta

### Observabilidade

- Actuator e métricas: `GET /actuator/metrics` e `GET /actuator/prometheus`
- Traces e logs: os logs incluem `traceId` e `spanId` quando disponíveis

## Detalhes de arquitetura

### Camadas do backend

- Controllers chamam apenas services. Consultas e mapeamento de resposta ficam em `PautaQueryService`.
- Services de comando lidam com escrita; repositórios ficam atrás dos limites de service.
- Testes unitários cobrem `PautaService`, `SessaoService` e `PautaQueryService`.

### Estrutura do frontend (TypeScript)

```
frontend/src
  api/          # Cliente HTTP + endpoints de pautas
  components/   # Componentes de UI (forms, cards, toasts)
  pages/        # Telas por rota
  routes/       # Configuração do roteamento
  types/        # Tipos TypeScript compartilhados
  test/         # Vitest + Testing Library
```

Rotas:
- `/` listagem de pautas
- `/pautas/nova` criação de pauta
- `/pautas/:id` detalhe de pauta

### Testes do frontend

```
cd frontend
npm run test
```
