package com.example.taskmanager;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Test
    void createAndRetrieveTask() {
        TaskRequest req = new TaskRequest();
        req.setTitle("Service Task");
        req.setDescription("Service Desc");
        req.setStatus(TaskStatus.IN_PROGRESS);
        TaskResponse created = taskService.create(req);
        assertThat(created.getId()).isNotNull();
        TaskResponse fetched = taskService.getById(created.getId());
        assertThat(fetched.getTitle()).isEqualTo("Service Task");
        assertThat(fetched.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    }
}

