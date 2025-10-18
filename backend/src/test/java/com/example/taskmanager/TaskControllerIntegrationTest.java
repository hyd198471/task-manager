package com.example.taskmanager;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.model.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void fullCrudLifecycle() {
        // Create
        TaskRequest req = new TaskRequest();
        req.setTitle("Test Task");
        req.setDescription("Desc");
        req.setStatus(TaskStatus.TODO);
        ResponseEntity<String> createResp = restTemplate.postForEntity("/api/tasks", req, String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).contains("Test Task");

        // List
        ResponseEntity<String> listResp = restTemplate.getForEntity("/api/tasks", String.class);
        assertThat(listResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listResp.getBody()).contains("Test Task");

        // Extract ID (simple parse)
        String body = listResp.getBody();
        assertThat(body).contains("id");
        String idStr = body.replaceAll(".*\\\"id\\\":(\\d+).*", "$1");
        Long id = Long.parseLong(idStr);

        // Update
        req.setTitle("Updated Task");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TaskRequest> updateEntity = new HttpEntity<>(req, headers);
        ResponseEntity<String> updateResp = restTemplate.exchange("/api/tasks/" + id, HttpMethod.PUT, updateEntity, String.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).contains("Updated Task");

        // Delete
        ResponseEntity<Void> deleteResp = restTemplate.exchange("/api/tasks/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(deleteResp.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Confirm 404
        ResponseEntity<String> get404 = restTemplate.getForEntity("/api/tasks/" + id, String.class);
        assertThat(get404.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}

