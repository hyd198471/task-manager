package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository repository;

    public TaskServiceImpl(TaskRepository repository) { this.repository = repository; }

    @Override
    public List<TaskResponse> getAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public TaskResponse getById(Long id) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        return toResponse(task);
    }

    @Override
    public TaskResponse create(TaskRequest request) {
        Task task = new Task();
        apply(task, request);
        return toResponse(repository.save(task));
    }

    @Override
    public TaskResponse update(Long id, TaskRequest request) {
        Task task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        apply(task, request);
        return toResponse(repository.save(task));
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new TaskNotFoundException(id);
        repository.deleteById(id);
    }

    private void apply(Task task, TaskRequest request) {
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), task.getStatus(), task.getDueDate());
    }
}

