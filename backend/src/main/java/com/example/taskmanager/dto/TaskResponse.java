package com.example.taskmanager.dto;

import com.example.taskmanager.model.TaskStatus;
import java.time.LocalDate;

public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private LocalDate dueDate;

    public TaskResponse(Long id, String title, String description, TaskStatus status, LocalDate dueDate) {
        this.id = id; this.title = title; this.description = description; this.status = status; this.dueDate = dueDate;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
}

