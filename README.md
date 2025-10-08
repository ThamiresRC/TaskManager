# 🚀 TaskManager – Deploy no Azure (App Service + SQL + GitHub Actions)

Guia passo a passo com tudo que usei e que **deu certo** para publicar o TaskManager no Azure.

---

## ✅ Visão geral

- **App Service**: `tm-fiap-taskmanager-v2` (Linux, Java 17, Brazil South, Plano B1)
- **Banco**: `db-taskmanager-v2` no servidor `sql-taskmanager-v2`
- **CI/CD**: GitHub Actions com **Publish Profile**
- **Application Insights**: Habilitado (Live Metrics/Logs)
- **URL de produção**: copie do **Domínio padrão** na Visão Geral do App Service  
  Ex.: `https://tm-fiap-taskmanager-v2-xxxxxxxx.brazilsouth-01.azurewebsites.net`

---

## 🧰 Pré-requisitos

- Java 17 e Maven (ou wrapper `mvnw`)
- Repositório no GitHub
- Conta Azure com permissão para criar recursos

---

## 1) Criar recursos no Azure (Portal)

1. **App Service Plan** (Linux, **B1**, região **Brazil South**).
2. **App Service**
  - Nome: `tm-fiap-taskmanager-v2`
  - **Runtime**: Java 17 + Java SE (Embedded Web Server)
  - Plano: o criado acima
  - (opcional) **Always On**: **Habilitar** (reduz cold start)
3. **Application Insights** (Brazil South) e vincular ao App Service.
4. **Azure SQL**
  - **Servidor**: `sql-taskmanager-v2` (usuário e senha fortes)
  - **Banco**: `db-taskmanager-v2`
  - Em **Rede (Firewall)** do servidor SQL:
    - **Permitir serviços do Azure**: **Ativado**
    - (opcional) Adicionar seu IP para testes locais

---

## 2) Criar o DDL (tabelas)

Crie no projeto a pasta `sql/` e o arquivo `ddl_taskmanager.sql`:

```sql
-- DDL TaskManager (SQL Server / Azure SQL)

IF OBJECT_ID(N'dbo.task', N'U') IS NULL
BEGIN
  CREATE TABLE dbo.task (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(2000) NULL,
    status NVARCHAR(20) NOT NULL,
    due_date DATE NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
  );
END;

IF OBJECT_ID(N'dbo.audit_log', N'U') IS NULL
BEGIN
  CREATE TABLE dbo.audit_log (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    task_id BIGINT NOT NULL,
    action NVARCHAR(30) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_audit_task FOREIGN KEY (task_id) REFERENCES dbo.task(id)
  );
END;
```

Execute esse script no **Query editor** do Azure SQL (ou via SSMS/Azure Data Studio).

---

## 3) Dependência do driver SQL Server (pom)

No `pom.xml`, garanta a dependência do driver para Java 17:

```xml
<dependency>
  <groupId>com.microsoft.sqlserver</groupId>
  <artifactId>mssql-jdbc</artifactId>
  <version>12.4.2.jre11</version> <!-- jre11+ atende o Java 17 -->
</dependency>
```

---

## 4) Variáveis do App Service (Portal)

No App Service → **Configurações > Variáveis de ambiente** → **Adicionar**:

| Nome                          | Valor (exemplo)                                                                                                                                         |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------|
| `SPRING_DATASOURCE_URL`      | `jdbc:sqlserver://sql-taskmanager-v2.database.windows.net:1433;database=db-taskmanager-v2;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;` |
| `SPRING_DATASOURCE_USERNAME` | `<login>@sql-taskmanager-v2` (ex: `taskadmin@sql-taskmanager-v2`)                                                                                       |
| `SPRING_DATASOURCE_PASSWORD` | `********` (senha do servidor SQL)                                                                                                                      |
| `SPRING_JPA_DATABASE_PLATFORM` | `org.hibernate.dialect.SQLServerDialect`                                                                                                             |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `update` *(ou `none` se criou tudo via DDL)*                                                                                                        |
| `SPRING_SQL_INIT_MODE`       | `never`                                                                                                                                                  |

Depois de **Salvar**, clique em **Reiniciar** o App Service.

---

## 5) Configurar o Publish Profile no GitHub

1. No App Service → **Baixar o perfil de publicação** (arquivo `.PublishSettings`).
2. No GitHub do projeto → **Settings → Secrets and variables → Actions → New repository secret**
  - **Name**: `AZURE_WEBAPP_PUBLISH_PROFILE`
  - **Secret**: conteúdo do `.PublishSettings` (abra no bloco de notas e cole tudo).

---

## 6) Workflow do GitHub Actions

Crie a pasta `.github/workflows/` e o arquivo `deploy.yml`:

```yaml
name: Deploy to Azure Web App

on:
  push:
    branches: [ "main" ]
  workflow_dispatch:

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: maven

      - name: Build with Maven
        run: |
          chmod +x mvnw || true
          ./mvnw -B -DskipTests clean package

      - name: Deploy to Azure Web App
        uses: azure/webapps-deploy@v3
        with:
          app-name: tm-fiap-taskmanager-v2
          publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
          package: target/*.jar
```

> **Dica (PowerShell)**: se preferir, você pode gerar esse arquivo via here-string:
>
> ```powershell
> @'
> # (cole aqui o conteúdo YAML acima)
> '@ | Set-Content -Encoding UTF8 .github/workflows/deploy.yml
> ```

---

## 7) Git – comandos usados

```bash
# adicionar e commitar
git add .
git commit -m "CI/CD: deploy para App Service v2 usando publish profile"

# se der rejeição (precisa puxar o que está no remoto)
git stash -u -m "temp-rebase"
git pull --rebase origin main
git stash pop

# push final
git push origin main
```

O **push na main** dispara o workflow. Acompanhe em **GitHub → Actions**.

---

## 8) Testar

- Abra a URL do App Service (Visão geral → **Domínio padrão**), por exemplo:  
  `https://tm-fiap-taskmanager-v2-xxxxxxxx.brazilsouth-01.azurewebsites.net/tasks`

> **Primeiro acesso pode demorar** (cold start). Ative **Always On** para melhorar.

---

## 9) Observabilidade

- App Service → **Log de stream** (para ver exceções ao vivo)
- Application Insights → **Live Metrics** e **Logs (KQL)**
- Exemplo de consulta:
  ```kusto
  requests
  | where timestamp > ago(15m)
  | project timestamp, name, resultCode, url
  | order by timestamp desc
  ```

---

## 10) Problemas comuns (checklist rápido)

- `SQLServerException`  
  → Revise `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` e **Firewall** do SQL (permitir serviços do Azure).
- `ClassNotFoundException: com.microsoft.sqlserver.jdbc.SQLServerDriver`  
  → Falta do driver no `pom.xml`.
- **Lento no primeiro acesso**  
  → Habilite **Always On** e evite reiniciar o App Service com frequência.
- **5xx subindo na métrica**  
  → Veja o **Log de stream** para a causa raiz e a consulta de **requests** no Insights.

---

## 11) Endpoints úteis

- UI principal: `GET /tasks`
- (Se tiver Actuator): `GET /actuator/health`

---

## 12) Estrutura do projeto (resumo)

```
.
├─ sql/
│  └─ ddl_taskmanager.sql
├─ src/main/java/br/com/fiap/taskmanager/...
├─ pom.xml
└─ .github/workflows/deploy.yml
```

---

## ✅ Pronto!

Qualquer ajuste de configuração é só alterar as **variáveis do App Service** (Portal) e **Reiniciar**.  
Ao mudar código, **push na `main`** e o Actions cuida do deploy.
