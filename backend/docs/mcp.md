# MCP Service for Task Manager

## Overview
This MCP-compatible layer exposes schema inspection, controlled batch inserts (max 1000), and summary statistics for tasks. All routes require a bearer token via `Authorization: Bearer <MCP_TOKEN>`.

Base path: `/api/mcp`

## Tools / Endpoints
- `GET /api/mcp/mcp-schema-tasks` — JSON Schema for Task requests (title required, status enum, max lengths, dueDate format date)
- `POST /api/mcp/mcp-tasks` — Insert an array of TaskRequest (max 1000 items). Defaults status to `TODO` when omitted.
- `GET /api/mcp/mcp-tasks-summary` — Totals and per-status counts.
- `GET /api/mcp/mcp-help` — Brief descriptions.
- `GET /api/mcp/mcp-spec` — Advertises `specVersion: 2025-06-18` and tool list.

## Auth
- Set `MCP_TOKEN` env variable on the backend. All `/api/mcp/**` calls must present `Authorization: Bearer <MCP_TOKEN>`.

## OpenAPI / Swagger
- Swagger UI: `/swagger-ui/index.html`
- OpenAPI JSON: `/v3/api-docs`

## Sample cURL Flow (schema → insert 1000 → summary)
```bash
TOKEN=your-demo-token
BASE=http://localhost:8080/api/mcp

# 1) Fetch schema
curl -H "Authorization: Bearer $TOKEN" $BASE/mcp-schema-tasks | jq

# 2) Generate 1000 tasks client-side (example using jq & python shown as placeholder)
python - <<'PY'
import json, random, datetime
statuses = ["TODO","IN_PROGRESS","DONE"]
items = []
for i in range(1000):
    items.append({
        "title": f"Task {i} example",
        "description": f"Sample description {i}",
        "status": random.choice(statuses),
        "dueDate": (datetime.date.today() + datetime.timedelta(days=random.randint(1,90))).isoformat()
    })
print(json.dumps(items))
PY > /tmp/tasks.json

# 3) Insert batch (max 1000)
curl -X POST -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  --data @/tmp/tasks.json \
  $BASE/mcp-tasks | jq '.[0:3]'

# 4) Verify summary
curl -H "Authorization: Bearer $TOKEN" $BASE/mcp-tasks-summary | jq
```

## Logging
MCP insert/generate actions log to console with counts (e.g., `mcp-tasks inserted count=1000`).

## Success Criteria Checklist
- MCP routes require bearer token and respond per specVersion `2025-06-18`.
- `mcp-schema-tasks` returns schema.
- `mcp-tasks` accepts up to 1000 tasks and inserts them.
- `mcp-tasks-summary` reflects inserted totals.
- Logs show inserted count for auditing.

