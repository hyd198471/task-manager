package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;

import java.util.List;

public interface TaskService {
    List<TaskResponse> getAll();
    TaskResponse getById(Long id);
    TaskResponse create(TaskRequest request);
    TaskResponse update(Long id, TaskRequest request);
    void delete(Long id);
}

