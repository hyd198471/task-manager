# Task Manager Full-Stack Application

This project is a simple full-stack Task Manager web application demonstrating CRUD operations with a React + TypeScript frontend and a Spring Boot backend using an in-memory H2 database.

## Tech Stack
- Backend: Spring Boot (Java 17), Maven, H2, JPA, Bean Validation
- Frontend: React 18 + TypeScript + Vite, Axios
- Testing: JUnit 5 (backend), Vitest/React Testing Library (frontend)

## Domain Model
Task {
  id: number (auto-generated)
  title: string (required, max 100 chars)
  description: string (optional, max 500 chars)
  status: enum [TODO, IN_PROGRESS, DONE]
  dueDate: date (optional, yyyy-MM-dd)
}

## Backend
Base URL: `http://localhost:8080/api`

Endpoints:
- GET /api/tasks
- GET /api/tasks/{id}
- POST /api/tasks
- PUT /api/tasks/{id}
- DELETE /api/tasks/{id}

Validation errors return HTTP 400 with a JSON body detailing field messages.

### Running Backend
```bash
cd backend
mvn spring-boot:run
```
H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:tasksdb`)

### Running Tests (Backend)
```bash
cd backend
mvn test
```

## Frontend
Dev server (Vite) runs on http://localhost:5173.

### Running Frontend
```bash
cd frontend
npm install
npm run dev
```

### Building Frontend
```bash
npm run build
```

### Frontend Tests
```bash
npm test
```

## Environment Configuration
Frontend uses `VITE_API_BASE` (default: http://localhost:8080/api).

Create a `.env` in `frontend` (optional):
```
VITE_API_BASE=http://localhost:8080/api
```

## Sorting & Editing
Tasks can be sorted by status or due date. Status can be changed via dropdown; editing opens a modal.

## Error Handling
Backend validation & not-found errors mapped to structured JSON. Frontend surfaces messages under the form and in a toast-like inline area.

## AI Usage Documentation
This project was co-developed with an AI assistant for:
- Scaffolding initial project structure (backend & frontend)
- Generating boilerplate CRUD code and validation annotations
- Implementing React components and custom hook for task state
- Creating integration & unit tests
- Designing error handling and CORS configuration
- Writing this README and usage instructions
- Iterative refinement & edge case consideration (validation, empty lists, error surfaces)

## Next Possible Enhancements
- Task categories & filtering
- Full-text search
- Pagination & large dataset performance
- Authentication & multi-user tenancy
- Deployment pipeline (CI/CD) + containerization

## Docker & Containerization
This project includes container definitions for both backend (Spring Boot) and frontend (React/Vite served by Nginx) plus a `docker-compose.yml` to orchestrate them.

### Build Images Individually
```bash
# Backend (from ./backend)
docker build -t task-manager-backend:latest backend

# Frontend (inject API base at build-time if different)
docker build --build-arg VITE_API_BASE=http://localhost:8080/api -t task-manager-frontend:latest frontend
```

### Run Containers Individually
```bash
# Run backend
docker run -d --name task-manager-backend -p 8080:8080 \
  -e APP_CORS_ALLOWEDORIGINS=http://localhost:5173 \
  task-manager-backend:latest

# Run frontend (port 5173 mapped to Nginx port 80)
docker run -d --name task-manager-frontend -p 5173:80 task-manager-frontend:latest
```
Access frontend at http://localhost:5173 and API at http://localhost:8080/api.

### Using Docker Compose
```bash
docker compose up --build
```
This will build both images (if not present) and start:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

Stop with:
```bash
docker compose down
```

### Environment & Configuration
- Backend CORS origins: set via `APP_CORS_ALLOWEDORIGINS` (maps to `app.cors.allowedOrigins`).
- Frontend API base: injected at build time via `--build-arg VITE_API_BASE=...` in Docker build.

If you need to point the frontend to a different backend host (e.g., staging), rebuild the frontend image with the new `VITE_API_BASE`.

### Development vs Production
For rapid local development you may still prefer running `mvn spring-boot:run` and `npm run dev`. Containers are suited for integration testing and deployment. The frontend image serves static content via Nginx (no hot reload). The backend image is a slim JRE using a non-root user for better security.

### Common Adjustments
- Persisting data: replace in-memory H2 with a persistent database (e.g., Postgres) and add a service in `docker-compose.yml`.
- Multi-arch builds: use `docker buildx build --platform linux/amd64,linux/arm64 ...`.
- Image tags: version images (e.g., `task-manager-backend:1.0.0`) instead of `latest` for CI/CD.

## Troubleshooting
### BuildKit / buildx cgroup error
If you see:
```
Error response from daemon: cgroup-parent for systemd cgroup should be a valid slice named as "xxx.slice"
```
This is a Docker BuildKit + buildx issue (common under some WSL2 / rootless setups) when Compose delegates builds. Workarounds:
1. Disable BuildKit temporarily (already provided via `.env`):
```bash
docker compose build
```
2. One-off without .env:
```bash
DOCKER_BUILDKIT=0 COMPOSE_DOCKER_CLI_BUILD=0 docker compose up --build
```
3. Force classic builder only for a single image:
```bash
DOCKER_BUILDKIT=0 docker build -t task-manager-backend:latest backend
```
Long-term fix: Update Docker Desktop / engine, ensure systemd + cgroup v2 properly configured, or recreate the builder:
```bash
docker buildx rm mybuilder 2>/dev/null || true
docker buildx create --use --name mybuilder
```

### Stale / broken frontend after nginx.conf fix
If Compose keeps using an old cached layer:
```bash
docker compose down
rm -f frontend/dist/** 2>/dev/null || true
DOCKER_BUILDKIT=0 docker compose build --no-cache frontend
docker compose up -d frontend
```
Make sure no standalone container is still binding the port:
```bash
docker ps --format '{{.Names}}\t{{.Ports}}'
```
If you see `tm-frontend-fix`, remove it before `docker compose up`:
```bash
docker rm -f tm-frontend-fix
```

### Cleaning everything
```bash
docker compose down -v --remove-orphans
docker rm -f tm-frontend-fix tm-backend-fix 2>/dev/null || true
# Prune dangling images (optional)
docker image prune -f
```

### Health checks (optional enhancement)
Add to `docker-compose.yml`:
```yaml
  backend:
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
    # (Also add spring-boot-starter-actuator & enable health endpoint.)
  frontend:
    healthcheck:
      test: ["CMD", "wget", "-qO-", "http://localhost/" ]
      interval: 30s
      timeout: 5s
      retries: 3
```

## License
MIT (adjust as needed).

**MCP Server**
- **Purpose**: Exposes a small set of developer-only tools that an AI agent can call to inspect the `tasks` schema, insert records, and retrieve simple summaries. These endpoints are intended for local development, testing, and automated data generation only.
- **Spec version**: `2025-06-18` (available via `GET /api/mcp/mcp-spec`).

- **Tools / Endpoints**:
  - **mcp-schema-tasks**: `GET /api/mcp/mcp-schema-tasks` — returns a simplified JSON-Schema for the `Task` object (properties: `title`, `description`, `status`, `dueDate`).
  - **mcp-tasks**: `POST /api/mcp/mcp-tasks` — accepts a JSON array of `TaskRequest` objects and inserts them into the DB. Each object should match the DTO: `title` (string, required, <=100 chars), `description` (string, optional, <=500 chars), `status` (`TODO|IN_PROGRESS|DONE`), `dueDate` (`YYYY-MM-DD`, optional).
  - **mcp-tasks-summary**: `GET /api/mcp/mcp-tasks-summary` — returns `{ "byStatus": {..}, "total": <n> }`.
  - **mcp-help**: `GET /api/mcp/mcp-help` — short map with endpoint descriptions.
  - **mcp-generate**: `POST /api/mcp/generate?count=N` — server-side convenience endpoint that generates N realistic tasks using Java Faker and inserts them.

**How it works**
- The MCP controller lives at `backend/src/main/java/com/example/taskmanager/mcp/` and delegates to `McpService`.
- `McpService` exposes helpers:
  - JSON Schema generation for `Task` (`getJsonSchemaForTasks()`)
  - Insert single/batch tasks (`insertTask`, `insertTasks`)
  - Generate realistic tasks using `javafaker` (`generateTestData`)
  - Summary statistics (`getSummary()`)

**Sample agent prompt (example)**
"Inspect the Task schema by calling `GET /api/mcp/mcp-schema-tasks`. Then generate 1000 realistic Task objects following the schema, POST them as a JSON array to `POST /api/mcp/mcp-tasks`, and finally call `GET /api/mcp/mcp-tasks-summary` to confirm that 1000 records were inserted. Return the summary JSON." 

**Recorded test: inserting 1000 records**
- Steps I ran locally (commands):
```bash
# start backend (or use docker compose)
# from repo root
cd backend
nohup mvn -DskipTests spring-boot:run > ../backend-run.log 2>&1 & echo $!

# inspect spec
curl -s http://localhost:8080/api/mcp/mcp-spec | jq .

# save schema
curl -s http://localhost:8080/api/mcp/mcp-schema-tasks | jq . > /tmp/mcp_schema.json

# insert 1000 generated tasks (client-side generator)
python3 scripts/insert_tasks.py 1000

# check summary
curl -s http://localhost:8080/api/mcp/mcp-tasks-summary | jq . > /tmp/mcp_summary.json
cat /tmp/mcp_summary.json
```

- Result (recorded): the summary returned by `/api/mcp/mcp-tasks-summary` after insertion was:

```json
{
  "byStatus": { "TODO": 325, "IN_PROGRESS": 343, "DONE": 332 },
  "total": 1000
}
```

**Saved artifacts / logs**
- Schema JSON: `/tmp/mcp_schema.json`
- Summary after insertion: `/tmp/mcp_summary.json`
- Backend run log: `backend-run.log` (created at project root when backend started with nohup)

**Security & recommendations**
- These MCP endpoints are intentionally unsafe for public exposure — they are unauthenticated developer tools. Before exposing them beyond a trusted network, add authentication (token or OAuth) and restrict access by environment.
- For deterministic generated data (repeatable tests), seed the `Faker` instance in `McpService` from an environment variable.
- For very large bulk loads, consider batching inserts and enabling JDBC batching for performance.

If you'd like, I can (pick one):
- Add a small token guard around `/api/mcp/**` (development-only) and document how to enable it.
- Make Faker deterministic via an env var and re-run the 1000-insert test to produce a deterministic sample.
- Commit the saved `/tmp/mcp_schema.json` and `/tmp/mcp_summary.json` into a `docs/` folder in the repo for easy reference.
