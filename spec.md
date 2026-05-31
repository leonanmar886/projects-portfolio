# SPEC — Sistema de Gerenciamento de Portfólio de Projetos

> **Stack:** Java · Spring Boot · Spring Security · JPA/Hibernate · PostgreSQL · Swagger/OpenAPI  
> **Arquitetura:** MVC (Controller → Service → Repository)  
> **Estilo de código:** Clean Code · SOLID · DTOs com mapeamento explícito

---

## 1. Visão Geral

Sistema REST para gerenciar o ciclo de vida completo de projetos corporativos, desde a análise de viabilidade até o encerramento. Contempla controle de status sequencial, classificação dinâmica de risco, alocação de membros com CRUD próprio, e geração de relatório de portfólio.

A API de membros **é parte do mesmo sistema**, exposta sob o prefixo `/api/members`. Não há dependência de serviço externo real; a separação exigida pelo enunciado é satisfeita pela distinção de camadas (controller/service/repository próprios para `Member`), preservando a possibilidade de extrair o módulo futuramente sem refatoração significativa.

---

## 2. Domínio

### 2.1 Entidade: `Project`

| Campo | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | `Long` | — | PK gerada |
| `name` | `String` | ✅ | |
| `startDate` | `LocalDate` | ✅ | |
| `estimatedEndDate` | `LocalDate` | ✅ | |
| `actualEndDate` | `LocalDate` | ❌ | Preenchido ao encerrar |
| `budget` | `BigDecimal` | ✅ | Positivo |
| `description` | `String` | ❌ | |
| `manager` | `Member` | ✅ | `@ManyToOne` — qualquer role pode ser gerente |
| `status` | `ProjectStatus` (enum) | ✅ | Padrão: `EM_ANALISE` |
| `members` | `Set<Member>` | — | `@ManyToMany` via tabela `project_members` |

**Risco calculado dinamicamente** (não persistido):

| Nível | Condição |
|---|---|
| `BAIXO` | budget ≤ 100.000 **E** prazo ≤ 3 meses |
| `MEDIO` | budget entre 100.001–500.000 **OU** prazo entre 3–6 meses |
| `ALTO` | budget > 500.000 **OU** prazo > 6 meses |

> Prazo = diferença em meses entre `startDate` e `estimatedEndDate`.  
> Casos de borda: exatamente 3 meses → `BAIXO` (≤); exatamente 100.000 → `BAIXO` (≤).

### 2.2 Enum: `ProjectStatus`

```
EM_ANALISE → ANALISE_REALIZADA → ANALISE_APROVADA → INICIADO → PLANEJADO → EM_ANDAMENTO → ENCERRADO
```

- `CANCELADO` pode ser aplicado a partir de **qualquer** status.
- Transições devem seguir a sequência estritamente. Pular etapas é proibido.
- Projetos com status `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO` **não podem ser excluídos**.

### 2.3 Entidade: `Member`

| Campo | Tipo | Obrigatório | Observação |
|---|---|---|---|
| `id` | `Long` | — | PK gerada |
| `name` | `String` | ✅ | |
| `role` | `MemberRole` (enum) | ✅ | `FUNCIONARIO` ou `GERENTE` |

### 2.4 Enum: `MemberRole`

| Valor enum | Valor string (API) | Pode ser associado a projeto |
|---|---|---|
| `FUNCIONARIO` | `"funcionario"` | ✅ |
| `GERENTE` | `"gerente"` | ❌ (pode ser gerente responsável, não membro alocado) |

> A serialização JSON usa o valor string em minúsculas para manter compatibilidade com o contrato do enunciado.

---

## 3. Módulo de Membros (`/api/members`)

Módulo independente dentro da mesma aplicação. Possui seu próprio controller, service e repository. O `ProjectService` interage com membros **exclusivamente via `MemberService`** — nunca via repositório direto — mantendo o isolamento entre módulos.

### 3.1 Endpoints

#### `POST /api/members`
Cria um novo membro.

**Request body:** `MemberCreateRequest`
```json
{ "name": "Ana Lima", "role": "funcionario" }
```

**Validações:**
- `name` não vazio
- `role` deve ser `"funcionario"` ou `"gerente"` (case-insensitive na entrada, normalizado internamente)

**Response `201`:** `MemberResponse`
```json
{ "id": 1, "name": "Ana Lima", "role": "funcionario" }
```

**Response `400`:** validação falhou

---

#### `GET /api/members`
Lista todos os membros. Suporta paginação e filtro por role.

**Query params:**

| Param | Tipo | Descrição |
|---|---|---|
| `page` | int | Padrão 0 |
| `size` | int | Padrão 20 |
| `role` | `MemberRole` | Filtro opcional |
| `name` | String | Filtro parcial (ILIKE) |

**Response `200`:** `Page<MemberResponse>`

---

#### `GET /api/members/{id}`
Busca membro por ID.

**Response `200`:** `MemberResponse`  
**Response `404`:** membro não encontrado

---

#### `PUT /api/members/{id}`
Atualiza nome e/ou role.

**Request body:** `MemberUpdateRequest`
```json
{ "name": "Ana Lima Silva", "role": "gerente" }
```

**Regra extra:** se o membro tiver `role` alterada de `FUNCIONARIO` para `GERENTE`, verificar se ele está alocado em algum projeto ativo. Se sim, retornar `409` com mensagem explicativa — não é possível mudar o role de um membro alocado.

**Response `200`:** `MemberResponse`  
**Response `409`:** membro alocado, mudança de role bloqueada

---

#### `DELETE /api/members/{id}`
Remove membro.

**Regra:** não pode excluir membro que seja gerente responsável de algum projeto ativo (status ≠ `ENCERRADO` e ≠ `CANCELADO`), nem membro alocado em algum projeto ativo.

**Response `204`:** sucesso  
**Response `409`:** membro vinculado a projetos ativos

---

### 3.2 Camadas internas do módulo

```
MemberController  →  MemberService  →  MemberRepository
```

- `MemberRepository` estende `JpaRepository<Member, Long>` e `JpaSpecificationExecutor<Member>`.
- `MemberService` expõe métodos públicos que o `ProjectService` pode usar:
    - `Member getById(Long id)` — lança `MemberNotFoundException` se não encontrado
    - `boolean existsById(Long id)`
    - `int countActiveProjectsForMember(Long memberId)` — delegado ao `ProjectRepository`

---

## 4. Endpoints de Projetos (`/api/projects`)

### `POST /api/projects`
Cria um projeto. Status inicial = `EM_ANALISE`.

**Request body:** `ProjectCreateRequest`
```json
{
  "name": "Sistema de RH",
  "startDate": "2025-01-01",
  "estimatedEndDate": "2025-06-30",
  "budget": 250000.00,
  "description": "Modernização do sistema de RH",
  "managerId": 3
}
```

**Validações:**
- `startDate` < `estimatedEndDate`
- `budget` > 0
- `managerId` deve existir (via `MemberService.getById`)

**Response `201`:** `ProjectResponse`

---

### `GET /api/projects`
Lista projetos com paginação e filtros.

**Query params:**

| Param | Tipo | Descrição |
|---|---|---|
| `page` | int | Padrão 0 |
| `size` | int | Padrão 10 |
| `status` | `ProjectStatus` | Filtro opcional |
| `managerId` | Long | Filtro opcional |
| `riskLevel` | `RiskLevel` | Filtro opcional (calculado pós-query, aplicado em memória) |
| `name` | String | Filtro parcial (ILIKE) |

**Response `200`:** `Page<ProjectResponse>`

> **Nota de implementação:** `riskLevel` não é persistido, portanto o filtro por risco deve ser aplicado após a consulta. Para evitar carregamento total, aplicar com paginação conservadora ou via query nativa se performance for crítica.

---

### `GET /api/projects/{id}`
**Response `200`:** `ProjectResponse`  
**Response `404`:** projeto não encontrado

---

### `PUT /api/projects/{id}`
Atualiza campos editáveis. Não altera status nem membros alocados.

**Request body:** `ProjectUpdateRequest`
```json
{
  "name": "Sistema de RH v2",
  "startDate": "2025-01-01",
  "estimatedEndDate": "2025-09-30",
  "actualEndDate": null,
  "budget": 300000.00,
  "description": "Escopo ampliado",
  "managerId": 2
}
```

**Response `200`:** `ProjectResponse`

---

### `DELETE /api/projects/{id}`
**Response `204`:** sucesso  
**Response `409`:** status é `INICIADO`, `EM_ANDAMENTO` ou `ENCERRADO`

---

### `PATCH /api/projects/{id}/status`
Avança o status do projeto.

**Request body:**
```json
{ "status": "ANALISE_REALIZADA" }
```

**Regras:**
- Novo status deve ser o imediatamente seguinte na sequência **ou** `CANCELADO`.
- Ao transicionar para `ENCERRADO`, preencher `actualEndDate` com `LocalDate.now()` se não estiver preenchido.

**Response `200`:** `ProjectResponse`  
**Response `422`:** transição inválida

---

### `POST /api/projects/{id}/members`
Associa um membro (funcionário) ao projeto.

**Request body:**
```json
{ "memberId": 5 }
```

**Validações (em ordem):**
1. Projeto existe → senão `404`
2. Membro existe → senão `404`
3. `member.role == FUNCIONARIO` → senão `422` (RN-06)
4. Projeto tem menos de 10 membros → senão `422` (RN-04)
5. Membro não está em mais de 3 projetos ativos → senão `422` (RN-05)
6. Membro não está já alocado neste projeto → senão `409`

**Response `200`:** `ProjectResponse`

---

### `DELETE /api/projects/{id}/members/{memberId}`
Remove membro do projeto.

**Regra:** se o projeto estiver em status `INICIADO`, `PLANEJADO` ou `EM_ANDAMENTO`, deve ter ao menos 1 membro após remoção.

**Response `200`:** `ProjectResponse`  
**Response `422`:** remoção deixaria projeto sem membros em status ativo

---

### `GET /api/projects/report`
Gera relatório resumido do portfólio.

**Response `200`:** `PortfolioReportResponse`
```json
{
  "countByStatus": {
    "EM_ANALISE": 3,
    "INICIADO": 2,
    "ENCERRADO": 5
  },
  "budgetByStatus": {
    "EM_ANALISE": 150000.00,
    "ENCERRADO": 1200000.00
  },
  "averageClosedDurationDays": 87.5,
  "totalUniqueMembers": 14
}
```

> `averageClosedDurationDays` = média de (`actualEndDate` - `startDate`) em dias dos projetos com status `ENCERRADO`.  
> `totalUniqueMembers` = contagem de IDs únicos em `project_members` (independente de status).

---

## 5. DTOs

### `MemberResponse`
```json
{ "id": 1, "name": "Ana Lima", "role": "funcionario" }
```

### `ProjectResponse`
```json
{
  "id": 1,
  "name": "Sistema de RH",
  "startDate": "2025-01-01",
  "estimatedEndDate": "2025-06-30",
  "actualEndDate": null,
  "budget": 250000.00,
  "description": "string",
  "manager": { "id": 3, "name": "Carlos Melo", "role": "gerente" },
  "status": "EM_ANDAMENTO",
  "riskLevel": "MEDIO",
  "members": [
    { "id": 5, "name": "Ana Lima", "role": "funcionario" }
  ]
}
```

> `riskLevel` calculado em `ProjectService` e injetado no DTO via `ProjectMapper`.

### `PortfolioReportResponse`
```json
{
  "countByStatus": { "EM_ANALISE": 3 },
  "budgetByStatus": { "EM_ANALISE": 150000.00 },
  "averageClosedDurationDays": 87.5,
  "totalUniqueMembers": 14
}
```

---

## 6. Segurança

- Spring Security com **usuário em memória** (hardcoded via `application.properties`).
- Autenticação HTTP Basic em todos os endpoints.
- Credenciais padrão:
    - `admin` / `admin123`
- Swagger UI (`/swagger-ui.html`, `/v3/api-docs`) liberado sem autenticação.

---

## 7. Estrutura de Pacotes

```
com.portfolio
├── config/
│   ├── SecurityConfig.java
│   └── SwaggerConfig.java
│
├── member/                              ← módulo autônomo
│   ├── controller/
│   │   └── MemberController.java
│   ├── service/
│   │   └── MemberService.java
│   ├── repository/
│   │   └── MemberRepository.java
│   ├── domain/
│   │   ├── Member.java
│   │   └── MemberRole.java
│   ├── dto/
│   │   ├── MemberCreateRequest.java
│   │   ├── MemberUpdateRequest.java
│   │   └── MemberResponse.java
│   ├── mapper/
│   │   └── MemberMapper.java
│   └── exception/
│       └── MemberNotFoundException.java
│
├── project/                             ← módulo principal
│   ├── controller/
│   │   └── ProjectController.java
│   ├── service/
│   │   └── ProjectService.java
│   ├── repository/
│   │   └── ProjectRepository.java
│   ├── domain/
│   │   ├── Project.java
│   │   └── ProjectStatus.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── ProjectCreateRequest.java
│   │   │   ├── ProjectUpdateRequest.java
│   │   │   ├── StatusChangeRequest.java
│   │   │   └── MemberAssociationRequest.java
│   │   └── response/
│   │       ├── ProjectResponse.java
│   │       └── PortfolioReportResponse.java
│   ├── mapper/
│   │   └── ProjectMapper.java
│   └── exception/
│       ├── ProjectNotFoundException.java
│       ├── InvalidStatusTransitionException.java
│       └── MemberAllocationException.java
│
├── shared/
│   ├── enums/
│   │   └── RiskLevel.java
│   ├── util/
│   │   └── RiskCalculator.java
│   └── exception/
│       └── GlobalExceptionHandler.java
```

> `ProjectService` importa `MemberService` via injeção de dependência. Nunca acessa `MemberRepository` diretamente.

---

## 8. Modelo de Dados (DDL referência)

```sql
CREATE TABLE members (
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    role    VARCHAR(50)  NOT NULL
);

CREATE TABLE projects (
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(255)   NOT NULL,
    start_date          DATE           NOT NULL,
    estimated_end_date  DATE           NOT NULL,
    actual_end_date     DATE,
    budget              NUMERIC(19,2)  NOT NULL,
    description         TEXT,
    manager_id          BIGINT         NOT NULL REFERENCES members(id),
    status              VARCHAR(50)    NOT NULL DEFAULT 'EM_ANALISE'
);

CREATE TABLE project_members (
    project_id  BIGINT NOT NULL REFERENCES projects(id),
    member_id   BIGINT NOT NULL REFERENCES members(id),
    PRIMARY KEY (project_id, member_id)
);
```

---

## 9. Regras de Negócio (para testes)

### RN-01: Transição de status
```
Dado projeto com status X
Quando PATCH /status com Y
Então aceitar se Y == próximo(X) ou Y == CANCELADO
Caso contrário 422
```

Mapeamento de transições válidas:
```
EM_ANALISE         → ANALISE_REALIZADA
ANALISE_REALIZADA  → ANALISE_APROVADA
ANALISE_APROVADA   → INICIADO
INICIADO           → PLANEJADO
PLANEJADO          → EM_ANDAMENTO
EM_ANDAMENTO       → ENCERRADO
qualquer           → CANCELADO  (exceto ENCERRADO e CANCELADO)
```

### RN-02: Proteção de exclusão
```
INICIADO | EM_ANDAMENTO | ENCERRADO → DELETE retorna 409
Demais status → DELETE permitido
```

### RN-03: Classificação de risco
```
budget=80.000,  prazo=2 meses  → BAIXO
budget=80.000,  prazo=3 meses  → BAIXO  (borda: ≤ 3)
budget=80.000,  prazo=4 meses  → MEDIO
budget=100.000, prazo=2 meses  → BAIXO  (borda: ≤ 100k)
budget=100.001, prazo=2 meses  → MEDIO
budget=200.000, prazo=2 meses  → MEDIO
budget=600.000, prazo=2 meses  → ALTO
budget=80.000,  prazo=8 meses  → ALTO
```

### RN-04: Limite de membros por projeto
```
Dado projeto com 10 membros
Quando POST /projects/{id}/members
Então 422 "Projeto já possui o número máximo de membros (10)"
```

### RN-05: Limite de projetos por membro
```
Dado membro em 3 projetos ativos (status != ENCERRADO && != CANCELADO)
Quando POST /projects/{id}/members
Então 422 "Membro já está alocado no número máximo de projetos simultâneos (3)"
```

### RN-06: Restrição de role na alocação
```
Dado membro com role == GERENTE
Quando POST /projects/{id}/members
Então 422 "Apenas funcionários podem ser associados a projetos"
```

### RN-07: Mudança de role bloqueada
```
Dado membro FUNCIONARIO alocado em projeto ativo
Quando PUT /members/{id} com role = "gerente"
Então 409 "Não é possível alterar o cargo de um membro alocado em projetos ativos"
```

### RN-08: Exclusão de membro bloqueada
```
Dado membro que é gerente de projeto ativo OU está alocado em projeto ativo
Quando DELETE /members/{id}
Então 409 "Membro está vinculado a projetos ativos e não pode ser removido"
```

---

## 10. Tratamento de Exceções

| Exceção | HTTP | Cenário |
|---|---|---|
| `ProjectNotFoundException` | 404 | Projeto não existe |
| `MemberNotFoundException` | 404 | Membro não existe |
| `InvalidStatusTransitionException` | 422 | RN-01 |
| `MemberAllocationException` | 422 | RN-04, RN-05, RN-06 |
| `ProjectDeletionException` | 409 | RN-02 |
| `MemberRoleChangeException` | 409 | RN-07 |
| `MemberDeletionException` | 409 | RN-08 |
| `MethodArgumentNotValidException` | 400 | Bean Validation |
| `Exception` (genérica) | 500 | Erro inesperado |

Envelope padrão de erro:
```json
{
  "timestamp": "2025-06-01T10:00:00",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Apenas funcionários podem ser associados a projetos",
  "path": "/api/projects/1/members"
}
```

---

## 11. Testes Unitários (≥ 70% de cobertura nas regras de negócio)

### `MemberService`
- Criação com dados válidos (funcionario e gerente)
- Criação com role inválida → 400
- Atualização de role bloqueada com membro alocado (RN-07)
- Atualização de role permitida com membro não alocado
- Exclusão bloqueada (membro alocado) (RN-08)
- Exclusão bloqueada (membro é gerente de projeto ativo) (RN-08)
- Exclusão permitida (membro sem vínculos ativos)

### `ProjectService`
- Criação com dados válidos
- Criação com `managerId` inexistente → 404
- Todas as 6 transições de status válidas (EM_ANALISE → ... → ENCERRADO)
- Todas as transições inválidas (pular etapa)
- `CANCELADO` aplicado em todos os status permitidos
- `actualEndDate` preenchido automaticamente ao encerrar
- Exclusão bloqueada nos 3 status proibidos (RN-02)
- Exclusão permitida nos demais

### `RiskCalculator`
- Todos os 8 cenários da RN-03 incluindo casos de borda

### `MemberAllocationValidator` (ou lógica em `ProjectService`)
- Associação válida: funcionário, projeto < 10, membro < 3 projetos ativos
- RN-04: projeto com 10 membros
- RN-05: membro em 3 projetos ativos
- RN-06: membro com role gerente

---

## 12. Configuração (`application.properties`)

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/portfolio
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

app.security.username=admin
app.security.password=admin123
```

---

## 13. Docker Compose

```yaml
version: '3.8'
services:
  db:
    image: postgres:15
    environment:
      POSTGRES_DB: portfolio
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

volumes:
  pgdata:
```

---

## 14. Checklist de Entrega

- [ ] CRUD completo de membros (`/api/members`)
- [ ] CRUD completo de projetos (`/api/projects`)
- [ ] Classificação de risco dinâmica
- [ ] Máquina de estados com validação de transição sequencial
- [ ] Associação/remoção de membros em projetos com todas as validações
- [ ] Endpoint de relatório de portfólio
- [ ] Paginação e filtros em projetos e membros
- [ ] Spring Security (HTTP Basic, usuário em memória)
- [ ] Swagger/OpenAPI em `/swagger-ui.html`
- [ ] Tratamento global de exceções com envelope padronizado
- [ ] Testes unitários ≥ 70% de cobertura nas regras de negócio
- [ ] `docker-compose.yml` para PostgreSQL
- [ ] `README.md` com instruções de execução
- [ ] Repositório público no GitHub (sem a palavra proibida em nenhum lugar)