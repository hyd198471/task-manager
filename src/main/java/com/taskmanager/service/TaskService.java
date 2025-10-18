package com.taskmanager.service;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskStatus;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Get all tasks, ordered by status and due date
     */
    public List<Task> getAllTasks() {
        return taskRepository.findAllOrderByStatusAndDueDate();
    }

    /**
     * Get a task by ID
     */
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    /**
     * Create a new task
     */
    public Task createTask(Task task) {
        // Set default status if not provided
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.TODO);
        }
        return taskRepository.save(task);
    }

    /**
     * Update an existing task
     */
    public Task updateTask(Long id, Task taskDetails) {
        Task existingTask = getTaskById(id);

        // Update fields
        existingTask.setTitle(taskDetails.getTitle());
        existingTask.setDescription(taskDetails.getDescription());
        existingTask.setStatus(taskDetails.getStatus());
        existingTask.setDueDate(taskDetails.getDueDate());

        return taskRepository.save(existingTask);
    }

    /**
     * Update only the status of a task
     */
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task existingTask = getTaskById(id);
        existingTask.setStatus(status);
        return taskRepository.save(existingTask);
    }

    /**
     * Delete a task
     */
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }

    /**
     * Get tasks by status
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatusOrderByDueDateAsc(status);
    }

    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks() {
        return taskRepository.findOverdueTasks(LocalDate.now(), TaskStatus.DONE);
    }

    /**
     * Get tasks due today
     */
    public List<Task> getTasksDueToday() {
        return taskRepository.findByDueDate(LocalDate.now());
    }

    /**
     * Search tasks by title
     */
    public List<Task> searchTasksByTitle(String title) {
        return taskRepository.findByTitleContainingIgnoreCase(title);
    }

    /**
     * Get task count by status
     */
    public long getTaskCountByStatus(TaskStatus status) {
        return taskRepository.countByStatus(status);
    }
}