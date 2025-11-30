MCP Server - Instructions and Runbook

Spec
- specVersion: 2025-06-18

Available endpoints (base: /api/mcp)
- GET /mcp-spec
  - Returns { specVersion, tools: [...] }
- GET /mcp-schema-tasks
  - Returns a simplified JSON-Schema for Task objects
- POST /mcp-tasks
  - Accepts a JSON array of TaskRequest objects and inserts them
- GET /mcp-tasks-summary
  - Returns counts per status and total
- GET /mcp-help
  - Returns a short map describing endpoints
- POST /generate?count=N
  - Convenience endpoint: server-side generation & insert of N tasks using javafaker

Agent workflow example (AI agent prompt example):
1) Inspect schema
- Request: GET http://localhost:8080/api/mcp/mcp-schema-tasks
- Expect: JSON Schema-like object with properties `title`, `description`, `status`, `dueDate` and `title` required.

2) Generate 1000 tasks (agent-side generation)
- The agent should build an array of 1000 TaskRequest objects. Each object should match DTO fields:
  {
    "title": "Short title up to 100 chars",
    "description": "Longer text up to 500 chars",
    "status": "TODO|IN_PROGRESS|DONE",
    "dueDate": "YYYY-MM-DD" // optional
  }

3) Submit tasks via mcp-tasks
- Request: POST http://localhost:8080/api/mcp/mcp-tasks
- Body: JSON array of 1000 TaskRequest objects
- Response: array of inserted TaskResponse objects

4) Validate success
- Request: GET http://localhost:8080/api/mcp/mcp-tasks-summary
- Expect: { "byStatus": { ... }, "total": 1000 }

Commands used in the repo to run this flow locally

Run the application (development):
```bash
# from repo root
docker compose up --build
# or run backend locally
cd backend
mvn spring-boot:run
```

Quick curl examples
```bash
curl -s http://localhost:8080/api/mcp/mcp-spec | jq .
curl -s http://localhost:8080/api/mcp/mcp-schema-tasks | jq .
# insert tasks example (small payload)
curl -s -X POST http://localhost:8080/api/mcp/mcp-tasks -H 'Content-Type: application/json' \
  -d '[{"title":"Example","description":"desc","status":"TODO"}]' | jq .
curl -s http://localhost:8080/api/mcp/mcp-tasks-summary | jq .
```

Notes & recommendations
- Security: The MCP endpoints are intended for development; add authentication before exposing to untrusted networks.
- Security: The MCP endpoints are intended for development; add authentication before exposing to untrusted networks.

Enabling a dev token guard
- You can enable a simple dev-only token guard by setting the `MCP_TOKEN` environment variable before starting the backend. When set, all requests to `/api/mcp/**` must include the header `X-MCP-Token: <value>` matching `MCP_TOKEN`.

Example (bash):
```bash
export MCP_TOKEN=supersecret
# start backend
cd backend
mvn -DskipTests spring-boot:run
# curl with the token
curl -H "X-MCP-Token: supersecret" http://localhost:8080/api/mcp/mcp-spec | jq .
```
Enabling a dev token guard
- You can enable a simple dev-only token guard by setting the `MCP_TOKEN` environment variable before starting the backend. When set, all requests to `/api/mcp/**` must include the header `X-MCP-Token: <value>` matching `MCP_TOKEN`.

Example (bash):
```bash
export MCP_TOKEN=supersecret
# start backend
cd backend
mvn -DskipTests spring-boot:run
# curl with the token
curl -H "X-MCP-Token: supersecret" http://localhost:8080/api/mcp/mcp-spec | jq .
```
- Determinism: To make generated data deterministic, modify `McpService` to seed the `Faker` instance from an env var.
- Performance: Inserting 1000 records via JPA `saveAll` is fine for dev; for much larger datasets consider batching with JDBC.

Recorded agent prompts / sample AI output (example):
- Prompt: "Inspect the schema and generate 1000 realistic Task objects, then POST them to /api/mcp/mcp-tasks and verify the summary shows 1000 tasks. Return the summary." 
- Agent actions: GET `/mcp-schema-tasks` -> build 1000 objects -> POST `/mcp-tasks` -> GET `/mcp-tasks-summary`
- Sample final result: { "byStatus": { "TODO": 512, "IN_PROGRESS": 320, "DONE": 168 }, "total": 1000 }

If you want, I can run the generation and insertion now and append the exact outputs below.

Run results (recorded)

1) Server spec check
```bash
curl -s http://localhost:8080/api/mcp/mcp-spec | jq .
```
Response:
```json
{
  "specVersion": "2025-06-18",
  "tools": ["mcp-schema-tasks","mcp-tasks","mcp-tasks-summary","mcp-help","mcp-generate"]
}
```

2) Schema inspection
```bash
curl -s http://localhost:8080/api/mcp/mcp-schema-tasks | jq . > /tmp/mcp_schema.json
cat /tmp/mcp_schema.json
```
Saved schema file: `/tmp/mcp_schema.json` (sample schema included earlier in this document).

3) Insert 1000 tasks (agent-style client POST)
```bash
python3 scripts/insert_tasks.py 1000
```
Script output:
```
Posting 1000 tasks to http://localhost:8080/api/mcp/mcp-tasks...
Response received. Length: 321657
Inserted: 1000
```

4) Validate summary
```bash
curl -s http://localhost:8080/api/mcp/mcp-tasks-summary | jq . > /tmp/mcp_summary.json
cat /tmp/mcp_summary.json
```
Saved summary file: `/tmp/mcp_summary.json`
Sample response:
```json
{
  "byStatus": { "TODO": 325, "IN_PROGRESS": 343, "DONE": 332 },
  "total": 1000
}
```

Files created during this run
- `/tmp/mcp_schema.json` — schema returned by `/mcp-schema-tasks`
- `/tmp/mcp_summary.json` — summary after inserting 1000 tasks

Notes
- The insertion was performed by a local script (`scripts/insert_tasks.py`) which generates simple random text for title/description and posts to `/api/mcp/mcp-tasks` so the insertion is performed via the required endpoint.

