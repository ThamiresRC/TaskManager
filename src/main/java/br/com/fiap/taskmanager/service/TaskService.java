package br.com.fiap.taskmanager.service;

import br.com.fiap.taskmanager.domain.AuditLog;
import br.com.fiap.taskmanager.domain.Status;
import br.com.fiap.taskmanager.domain.Task;
import br.com.fiap.taskmanager.repository.AuditLogRepository;
import br.com.fiap.taskmanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;
    private final AuditLogRepository auditLogRepository;

    public List<Task> listAllSorted() {
        return repo.findAllByOrderByStatusAscDueDateAscCreatedAtAsc();
    }

    public Task getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }

    @Transactional
    public Task create(@Valid Task task) {
        Task saved = repo.save(task);
        auditLogRepository.save(new AuditLog(saved, "CREATE"));
        return saved;
    }

    @Transactional
    public Task update(Long id, @Valid Task updated) {
        Task t = getById(id);
        t.setTitle(updated.getTitle());
        t.setDescription(updated.getDescription());
        t.setStatus(updated.getStatus());
        t.setDueDate(updated.getDueDate());
        Task saved = repo.save(t);
        auditLogRepository.save(new AuditLog(saved, "UPDATE"));
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Task t = getById(id);
        repo.deleteById(id);
        auditLogRepository.save(new AuditLog(t, "DELETE"));
    }

    @Transactional
    public Task moveTo(Long id, Status status) {
        Task t = getById(id);
        t.setStatus(status);
        Task saved = repo.save(t);
        auditLogRepository.save(new AuditLog(saved, "MOVE_" + status.name()));
        return saved;
    }
}
