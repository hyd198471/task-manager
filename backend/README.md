# Task Manager Backend

// ...existing content...

## MCP (Model Context Protocol) Layer
- Base: `/api/mcp`
- Auth: bearer token required via `Authorization: Bearer <MCP_TOKEN>` (configure `MCP_TOKEN` env var)
- Spec: `GET /api/mcp/mcp-spec` returns `specVersion` `2025-06-18` and tools
- Endpoints:
  - `GET /api/mcp/mcp-schema-tasks` — JSON Schema for TaskRequest
  - `POST /api/mcp/mcp-tasks` — batch insert (max 1000)
  - `GET /api/mcp/mcp-tasks-summary` — totals/per-status counts
  - `GET /api/mcp/mcp-help` — descriptions
- Swagger UI: `/swagger-ui/index.html`

### Quick start
```bash
# set token
export MCP_TOKEN=your-demo-token

# run backend
mvn spring-boot:run

# fetch schema
curl -H "Authorization: Bearer $MCP_TOKEN" http://localhost:8080/api/mcp/mcp-schema-tasks

# insert 1000 tasks (example generation in docs/mcp.md)
curl -X POST -H "Authorization: Bearer $MCP_TOKEN" -H "Content-Type: application/json" \
  --data @/tmp/tasks.json \
  http://localhost:8080/api/mcp/mcp-tasks

# summary
curl -H "Authorization: Bearer $MCP_TOKEN" http://localhost:8080/api/mcp/mcp-tasks-summary
```

See `docs/mcp.md` for detailed flow, sample generation script, and success criteria.

