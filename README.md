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
