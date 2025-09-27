package br.com.fiap.taskmanager.web;

import br.com.fiap.taskmanager.domain.Status;
import br.com.fiap.taskmanager.domain.Task;
import br.com.fiap.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService service;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("tasks", service.listAllSorted());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("newTask", new Task());
        return "tasks/list";
    }

    @PostMapping
    public String create(@ModelAttribute("newTask") @Valid Task task,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tasks", service.listAllSorted());
            model.addAttribute("statuses", Status.values());
            return "tasks/list";
        }
        service.create(task);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/move")
    public String move(@PathVariable Long id, @RequestParam("status") Status status) {
        service.moveTo(id, status);
        return "redirect:/tasks";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("task", service.getById(id));
        model.addAttribute("statuses", Status.values());
        return "tasks/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("task") @Valid Task task,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", Status.values());
            return "tasks/form";
        }
        service.update(id, task);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/tasks";
    }
}
