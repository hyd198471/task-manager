package com.example.taskmanager.mcp;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Arrays;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @GetMapping("/schema")
    public Map<String, Object> schema() {
        return mcpService.getSchema();
    }

    @GetMapping("/mcp-schema-tasks")
    public Map<String, Object> mcpSchemaTasks() {
        return mcpService.getJsonSchemaForTasks();
    }

    @GetMapping("/tasks")
    public List<TaskResponse> readTasks(@RequestParam(name = "limit", required = false, defaultValue = "100") int limit) {
        return mcpService.readTasks(limit);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> insertTask(@RequestBody TaskRequest req) {
        TaskResponse r = mcpService.insertTask(req);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/tasks/batch")
    public ResponseEntity<List<TaskResponse>> insertTasks(@RequestBody List<TaskRequest> reqs) {
        List<TaskResponse> r = mcpService.insertTasks(reqs);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/mcp-tasks")
    public ResponseEntity<List<TaskResponse>> mcpTasksInsert(@RequestBody List<TaskRequest> reqs) {
        List<TaskResponse> r = mcpService.insertTasks(reqs);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/mcp-tasks-summary")
    public Map<String, Object> mcpTasksSummary() {
        return mcpService.getSummary();
    }

    @GetMapping("/mcp-help")
    public Map<String, String> mcpHelp() {
        Map<String, String> help = new LinkedHashMap<>();
        help.put("mcp-schema-tasks", "GET /api/mcp/mcp-schema-tasks - returns simplified JSON-Schema for Task objects");
        help.put("mcp-tasks", "POST /api/mcp/mcp-tasks - accepts JSON array of TaskRequest and inserts them into DB");
        help.put("mcp-tasks-summary", "GET /api/mcp/mcp-tasks-summary - returns task counts per status and total");
        help.put("mcp-help", "GET /api/mcp/mcp-help - returns this help map");
        help.put("mcp-generate", "POST /api/mcp/generate?count=N - generate N realistic tasks and insert them (for convenience)");
        return help;
    }

    @GetMapping("/mcp-spec")
    public Map<String, Object> mcpSpec() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("specVersion", "2025-06-18");
        out.put("tools", Arrays.asList("mcp-schema-tasks", "mcp-tasks", "mcp-tasks-summary", "mcp-help", "mcp-generate"));
        return out;
    }

    @PostMapping("/generate")
    public ResponseEntity<List<TaskResponse>> generate(@RequestParam(name = "count", required = false, defaultValue = "10") int count) {
        List<TaskResponse> r = mcpService.generateTestData(count);
        return ResponseEntity.ok(r);
    }
}
