package com.example.taskmanager.mcp;

import com.example.taskmanager.dto.TaskRequest;
import com.example.taskmanager.dto.TaskResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpService mcpService;

    public McpController(McpService mcpService) {
        this.mcpService = mcpService;
    }

    @GetMapping("/mcp-schema-tasks")
    public Map<String, Object> mcpSchemaTasks() {
        return mcpService.getJsonSchemaForTasks();
    }

    @PostMapping("/mcp-tasks")
    public ResponseEntity<List<TaskResponse>> mcpTasksInsert(
            @RequestBody @NotEmpty List<@Valid TaskRequest> reqs) {
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
        help.put("mcp-tasks", "POST /api/mcp/mcp-tasks - accepts JSON array of TaskRequest and inserts them into DB (max 1000 items)");
        help.put("mcp-tasks-summary", "GET /api/mcp/mcp-tasks-summary - returns task counts per status and total");
        help.put("mcp-help", "GET /api/mcp/mcp-help - returns this help map");
        return help;
    }

    @GetMapping("/mcp-spec")
    public Map<String, Object> mcpSpec() {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("specVersion", "2025-06-18");
        out.put("tools", List.of("mcp-schema-tasks", "mcp-tasks", "mcp-tasks-summary", "mcp-help"));
        return out;
    }
}
