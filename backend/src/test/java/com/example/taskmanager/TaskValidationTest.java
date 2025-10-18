package com.example.taskmanager;

import com.example.taskmanager.dto.TaskRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskValidationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void blankTitleShouldFailValidation() {
        TaskRequest req = new TaskRequest();
        req.setTitle(" "); // blank
        ResponseEntity<String> resp = restTemplate.postForEntity("/api/tasks", req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resp.getBody()).contains("Title is required");
        assertThat(resp.getBody()).contains("fieldErrors");
    }
}

