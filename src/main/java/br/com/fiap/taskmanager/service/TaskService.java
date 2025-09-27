package br.com.fiap.taskmanager.service;

import br.com.fiap.taskmanager.domain.Status;
import br.com.fiap.taskmanager.domain.Task;
import br.com.fiap.taskmanager.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository repo;

    public List<Task> listAllSorted() {
        return repo.findAllByOrderByStatusAscDueDateAscCreatedAtAsc();
    }

    public Task getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Task not found"));
    }

    public Task create(@Valid Task task) { return repo.save(task); }

    public Task update(Long id, @Valid Task updated) {
        Task t = getById(id);
        t.setTitle(updated.getTitle());
        t.setDescription(updated.getDescription());
        t.setStatus(updated.getStatus());
        t.setDueDate(updated.getDueDate());
        return repo.save(t);
    }

    public void delete(Long id) { repo.deleteById(id); }

    public Task moveTo(Long id, Status status) {
        Task t = getById(id);
        t.setStatus(status);
        return repo.save(t);
    }
}
