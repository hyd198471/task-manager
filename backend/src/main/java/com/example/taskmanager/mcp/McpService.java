package com.example.taskmanager.mcp;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.TaskStatus;
import com.example.taskmanager.repository.TaskRepository;
import com.github.javafaker.Faker;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.transaction.Transactional;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class McpService {

    private static final Logger log = LoggerFactory.getLogger(McpService.class);
    private static final int MAX_BATCH = 1000;

    private final TaskRepository taskRepository;
    private final Faker faker = new Faker(new Random());

    public McpService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Map<String, Object> getJsonSchemaForTasks() {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("$schema", "http://json-schema.org/draft-07/schema#");
        schema.put("title", "Task");
        schema.put("type", "object");

        Map<String, Object> props = new LinkedHashMap<>();

        Map<String, Object> title = new LinkedHashMap<>();
        title.put("type", "string");
        title.put("maxLength", 100);
        props.put("title", title);

        Map<String, Object> description = new LinkedHashMap<>();
        description.put("type", "string");
        description.put("maxLength", 500);
        props.put("description", description);

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("type", "string");
        List<String> enums = Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.toList());
        status.put("enum", enums);
        props.put("status", status);

        Map<String, Object> dueDate = new LinkedHashMap<>();
        dueDate.put("type", "string");
        dueDate.put("format", "date");
        props.put("dueDate", dueDate);

        schema.put("properties", props);
        schema.put("required", Arrays.asList("title"));
        return schema;
    }

    public Map<String, Object> getSummary() {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (TaskStatus s : TaskStatus.values()) {
            long c = taskRepository.countByStatus(s);
            byStatus.put(s.name(), c);
        }
        out.put("byStatus", byStatus);
        out.put("total", taskRepository.count());
        return out;
    }

    public Map<String, Object> getSchema() {
        Map<String, Object> resp = new LinkedHashMap<>();
        Class<Task> cls = Task.class;
        resp.put("entity", cls.getSimpleName());
        List<Map<String, Object>> fields = new ArrayList<>();
        for (Field f : cls.getDeclaredFields()) {
            Map<String, Object> fm = new LinkedHashMap<>();
            fm.put("name", f.getName());
            fm.put("type", f.getType().getSimpleName());
            List<String> annotations = Arrays.stream(f.getAnnotations())
                    .map(a -> a.annotationType().getSimpleName())
                    .collect(Collectors.toList());
            fm.put("annotations", annotations);
            fields.add(fm);
        }
        resp.put("fields", fields);
        return resp;
    }

    public List<TaskResponse> readTasks(int limit) {
        if (limit <= 0) limit = 100;
        return taskRepository.findAll(PageRequest.of(0, limit)).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public TaskResponse insertTask(TaskRequest req) {
        Task t = new Task();
        t.setTitle(req.getTitle());
        t.setDescription(req.getDescription());
        t.setStatus(req.getStatus() != null ? req.getStatus() : TaskStatus.TODO);
        if (req.getDueDate() != null) {
            t.setDueDate(req.getDueDate());
        }
        Task saved = taskRepository.save(t);
        return toResponse(saved);
    }

    @Transactional
    public List<TaskResponse> insertTasks(List<TaskRequest> reqs) {
        if (reqs == null || reqs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task list cannot be empty");
        }
        if (reqs.size() > MAX_BATCH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Batch too large; max 1000");
        }
        List<Task> list = new ArrayList<>();
        for (TaskRequest r : reqs) {
            Task t = new Task();
            t.setTitle(r.getTitle());
            t.setDescription(r.getDescription());
            t.setStatus(r.getStatus() != null ? r.getStatus() : TaskStatus.TODO);
            if (r.getDueDate() != null) t.setDueDate(r.getDueDate());
            list.add(t);
        }
        List<Task> saved = taskRepository.saveAll(list);
        log.info("mcp-tasks inserted count={}", saved.size());
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public List<TaskResponse> generateTestData(int count) {
        if (count > MAX_BATCH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Generation limit is 1000");
        }
        List<Task> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Task t = new Task();
            String title = faker.lorem().sentence(3).replaceAll("\\.$", "");
            String desc = faker.lorem().paragraph();
            t.setTitle(title.length() > 100 ? title.substring(0, 100) : title);
            t.setDescription(desc.length() > 500 ? desc.substring(0, 500) : desc);
            TaskStatus[] statuses = TaskStatus.values();
            t.setStatus(statuses[faker.random().nextInt(statuses.length)]);
            // due date within next 90 days or null occasionally
            if (faker.random().nextInt(10) < 8) {
                t.setDueDate(LocalDate.now().plusDays(faker.random().nextInt(1, 90)));
            }
            list.add(t);
        }
        List<Task> saved = taskRepository.saveAll(list);
        log.info("mcp-generate inserted count={}", saved.size());
        return saved.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private TaskResponse toResponse(Task t) {
        return new TaskResponse(t.getId(), t.getTitle(), t.getDescription(), t.getStatus(), t.getDueDate());
    }
}
