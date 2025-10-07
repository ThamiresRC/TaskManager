-- DDL TaskManager (SQL Server / Azure SQL)
-- Tabela principal
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

-- Tabela de auditoria (relacionada a task)
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
