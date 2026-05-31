# Projects Portfolio

Aplicação Spring Boot para gerenciar projetos e membros de equipe.

## Visão geral

A API oferece operações CRUD para membros e projetos, além de endpoints de relatório e associação entre projeto e membros.

## Como executar

### Pré-requisitos

- Java 21+
- Maven Wrapper incluído no projeto

### Build

- Linux/macOS: `./mvnw package`
- Windows: `./mvnw.cmd package`

### Execução

- `java -jar target/projects-portfolio-0.0.1-SNAPSHOT.jar`

## Autenticação padrão

A aplicação usa Basic Auth com credenciais em memória:

- usuário: `admin`
- senha: `admin123`

## Endpoints principais

### Membros

- `POST /api/members`
- `GET /api/members?page&size&role&name`
- `GET /api/members/{id}`
- `PUT /api/members/{id}`
- `DELETE /api/members/{id}`

### Projetos

- `POST /api/projects`
- `GET /api/projects?page&size&status&managerId&riskLevel&name`
- `GET /api/projects/{id}`
- `PUT /api/projects/{id}`
- `DELETE /api/projects/{id}`
- `PATCH /api/projects/{id}/status`
- `POST /api/projects/{id}/members`
- `DELETE /api/projects/{id}/members/{memberId}`
- `GET /api/projects/report`

## Documentação da API

- OpenAPI: `/v3/api-docs`
- Swagger UI: `/swagger-ui.html`

## Observações

- O `riskLevel` é calculado dinamicamente por projeto e não é persistido.
- O filtro `riskLevel` é aplicado em memória após os filtros de banco, adequado para volumes moderados. Para grandes volumes, o ideal é implementar query dedicada no banco ou denormalização.
