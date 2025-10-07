package br.com.fiap.taskmanager.repository;

import br.com.fiap.taskmanager.domain.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
