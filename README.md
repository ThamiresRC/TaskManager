# 🧭 Projeto Diamante — Task Manager

Aplicação **Spring Boot 3** para gerenciamento de tarefas com três status (**TODO**, **IN_PROGRESS**, **DONE**), interface **Thymeleaf** elegante e persistência em **H2** (arquivo).

> **URL principal:** `http://localhost:8080/tasks`
> **H2 Console:** `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/taskdb`, usuário `SA`)

---

## 🗂️ Estrutura do Projeto

**Módulo:** `task-manager`

| Camada/Pasta    | Item/Arquivo                         | Tipo       | Descrição resumida                                                                 |
| ----------------| ------------------------------------ | ---------- | ---------------------------------------------------------------------------------- |
| `domain`        | `Task`, `Status`                     | Model      | Entidade de Tarefa (título, descrição, `dueDate`, `createdAt`, `updatedAt`, `status`). |
| `repository`    | `TaskRepository`                     | Spring Data JPA | Repositório JPA com ordenação por `status`, `dueDate`, `createdAt`.                |
| `service`       | `TaskService`                        | Service    | Regras de negócio: criar, editar, mover status, excluir.                           |
| `web`           | `TaskController`                     | MVC        | Controlador MVC: lista, formulário, ações `move`, `delete`, `edit`.                |
| `resources/templates/tasks` | `list.html`, `form.html` | View (Thymeleaf) | UI dark com layout em “kanban” (3 colunas) e formulário compacto e centralizado.   |
| `resources`     | `application.properties`, `data.sql` | Config/Seed | Configuração do H2 e seed opcional de dados.                                       |

> A aplicação é **server-side MVC** (sem API REST pública). As ações do CRUD são submissões de formulário.

---

## 🧪 Requisitos Técnicos (atendidos)

- **Java 17**, **Spring Boot 3.5.x**, **Maven**.
- Dependências:
  - `spring-boot-starter-web`
  - `spring-boot-starter-thymeleaf`
  - `spring-boot-starter-data-jpa`
  - `spring-boot-starter-validation`
  - `com.h2database:h2` (runtime)
  - `org.projectlombok:lombok`
- **Camadas** separadas: domain, repository, service, web.
- **Validações** no modelo (Bean Validation).
- **Persistência** com H2 em arquivo (`./data/taskdb`).
- **UI** agradável com CSS leve (PicoCSS + custom).

---

## 🔌 Configuração

### `src/main/resources/application.properties`
```properties
spring.datasource.url=jdbc:h2:file:./data/taskdb
spring.datasource.username=SA
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

> Se quiser começar “zerado” sempre, troque `ddl-auto=update` por `create-drop` (apenas para desenvolvimento).

### (Opcional) `src/main/resources/data.sql`
```sql
-- Exemplos de tarefas iniciais
insert into tasks (title, description, status, due_date, created_at, updated_at)
values ('Exemplo TODO', 'Descrição breve', 'TODO', current_date, current_timestamp, current_timestamp);
```

---

## ⚙️ Como Rodar Localmente

### 1) Clonar e entrar
```bash
git clone <url-do-repo>
cd task-manager
```

### 2) Rodar com Maven Wrapper
```bash
# Windows
mvnw spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### 3) Acessos rápidos
- **App (Kanban + Form):** `http://localhost:8080/tasks`
- **H2 Console:** `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/taskdb`
  - User: `SA` (sem senha)

---

## 📚 Rotas Principais (MVC)

| Método | Rota                    | Ação/Descrição                                       |
|------- |------------------------ |------------------------------------------------------|
| GET    | `/tasks`                | Lista tarefas por coluna (TODO / IN_PROGRESS / DONE) |
| POST   | `/tasks`                | Cria nova tarefa                                      |
| GET    | `/tasks/{id}/edit`      | Abre formulário de edição                             |
| POST   | `/tasks/{id}`           | Salva edição                                          |
| POST   | `/tasks/{id}/move`      | Move para `status` informado (`TODO`, `IN_PROGRESS`, `DONE`) |
| POST   | `/tasks/{id}/delete`    | Exclui tarefa                                         |

---

## 🎨 UI & UX

- **Formulário compacto e centralizado**, com campos: Título, Data de entrega (date), Status e Descrição.
- **Três colunas** estilo *kanban*:
  - **TODO** → botão *Iniciar*
  - **IN_PROGRESS** → botão *Concluir*
  - **DONE** → botão *Reabrir*
- **Badges** com datas (Entrega / Criada / Atualizada / Concluída).
- **Botões de ação**: Editar e Excluir em cada card.

> O visual utiliza **PicoCSS** + estilos customizados (gradientes sutis, sombras e cantos arredondados).

---

## 🧩 Boas Práticas & Organização

- **SRP (Single Responsibility):** cada camada com uma responsabilidade; `TaskService` concentra regras de negócio.
- **DIP (Dependency Inversion):** controladores dependem de interfaces/serviços, não de JPA diretamente.
- **Validação** via Bean Validation no modelo e no formulário.
- **Ordenação** definida no repositório para garantir consistência visual.

---

## 🧭 Diagrama (Mermaid)

```mermaid
flowchart LR
  UI["Thymeleaf Views<br/>/tasks, /tasks/{id}/edit"] --> C[TaskController]
  C --> S[TaskService]
  S --> R[TaskRepository (Spring Data JPA)]
  R --> DB[(H2 - file ./data/taskdb)]
```

---

## ✅ Checklist

- [x] Spring Boot 3 + Java 17
- [x] MVC com Thymeleaf (UI pronta)
- [x] CRUD de Tarefas + mudança de status (TODO/IN_PROGRESS/DONE)
- [x] Datas: `createdAt`, `updatedAt`, `dueDate`
- [x] H2 em arquivo + H2 Console habilitado
- [x] Validação de campos e mensagens amigáveis
- [x] Estilo dark, leve e **delicado** ✨

---

## 🚀 Próximos Passos (ideias)

- Filtro/pesquisa por título e status.
- Paginação (em listas extensas).
- API REST pública (JSON) para integrar com front SPA.
- Autenticação simples (Spring Security) para uso multiusuário.

---

**Feito com ♥ para o Projeto Diamante.**
Sinta-se à vontade para abrir *issues* e *PRs*!
## 🚀 Entrega – Checkpoint (Azure)

- **App Service:** tm-fiap-taskmanager (Brazil South)
- **URL de Produção:** https://tm-fiap-taskmanager-xxxxx.brazilsouth-01.azurewebsites.net/tasks
- **CI/CD:** GitHub Actions  
  - Build: .github/workflows/build.yml
  - Deploy: .github/workflows/deploy.yml
- **Banco:** Azure SQL (db-taskmanager)
  - **DDL no repositório:** sql/ddl_taskmanager.sql (tabelas 	ask e udit_log com FK)
- **Application Insights:** tm-fiap-taskmanager (Brazil South) – Live Metrics e Logs habilitados

### Como testar
1. Acesse **https://tm-fiap-taskmanager-xxxxx.brazilsouth-01.azurewebsites.net/tasks** e crie/atualize tarefas.  
2. Os dados persistem no **Azure SQL**.  
3. Telemetria disponível em **Application Insights → Live Metrics** e **Logs**.

