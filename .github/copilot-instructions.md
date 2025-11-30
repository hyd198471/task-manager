# AI Agent Instructions for Task Manager

## Project Architecture
Full-stack task management app with Spring Boot backend + React/TypeScript frontend using in-memory H2 database. Both services are containerized with Docker Compose orchestration.

**Core Domain**: Simple CRUD operations on `Task` entity with validation, status enum (TODO/IN_PROGRESS/DONE), and optional due dates.

## Key Patterns & Conventions

### Backend (Spring Boot)
- **Package Structure**: Follow `com.example.taskmanager.{controller,service,repository,model,dto,exception,config}` pattern
- **Validation**: Use Jakarta validation annotations (`@NotBlank`, `@Size`) on DTOs, not entities. Global exception handler maps validation errors to structured JSON with `fieldErrors` map
- **Error Handling**: Custom exceptions extend RuntimeException; `@RestControllerAdvice` returns consistent error format with `error`, `type`, and optional `fieldErrors` fields
- **CORS Configuration**: Uses Spring-managed `CorsFilter` with environment-configurable origins via `app.cors.allowedOrigins` property

### Frontend (React + TypeScript)
- **State Management**: Custom hooks pattern (`useTasks.ts`) for API state management with loading/error states
- **Error Display**: Centralized error formatting in `formatError()` function that extracts validation messages from backend responses
- **API Layer**: Axios-based client in `api.ts` with TypeScript interfaces matching backend DTOs
- **Testing**: Vitest + React Testing Library setup; prefer component integration tests over unit tests

## Development Workflows

### Local Development
```bash
# Backend (port 8080)
cd backend && mvn spring-boot:run

# Frontend (port 5173) 
cd frontend && npm run dev

# H2 Console: http://localhost:8080/h2-console (JDBC: jdbc:h2:mem:tasksdb)
```

### Testing
```bash
# Backend tests (JUnit 5 + TestRestTemplate integration tests)
cd backend && mvn test

# Frontend tests (Vitest)
cd frontend && npm test
```

### Docker Development
```bash
# Full stack with Docker Compose
docker compose up --build

# If BuildKit errors occur, use legacy builder:
DOCKER_BUILDKIT=0 docker compose up --build
```

## Critical Integration Points

### Environment Configuration
- **Frontend API Base**: Set via `VITE_API_BASE` env var, injected at Docker build time
- **Backend CORS**: Configure allowed origins via `APP_CORS_ALLOWEDORIGINS` env var for container deployment
- **Database**: H2 in-memory with console enabled for development; connection details in `application.properties`

### Request/Response Patterns
- **Validation Errors**: Backend returns HTTP 400 with `{error, fieldErrors: {field: message}, type: "VALIDATION_ERROR"}` structure
- **Not Found Errors**: HTTP 404 with `{error: message, type: "NOT_FOUND"}` format
- **Task Status**: Use enum values exactly as `TODO`, `IN_PROGRESS`, `DONE` (backend validates these)

## When Making Changes

### Adding New Endpoints
1. Create DTOs in `dto/` package with validation annotations
2. Add controller method with `@Valid @RequestBody` 
3. Update `GlobalExceptionHandler` for new exception types
4. Add corresponding frontend API function and type definitions

### Frontend Component Updates
- Always update the `useTasks` hook for state changes
- Use the centralized `formatError` for consistent error display
- Maintain TypeScript interface alignment with backend DTOs

### Docker/Environment Changes
- Frontend API base requires image rebuild (no runtime config)
- Backend CORS changes can use environment variables at runtime
- Use `.env` file for local Docker Compose overrides

## Testing Conventions
- **Backend**: Integration tests with `@SpringBootTest` and `TestRestTemplate` for full HTTP flows
- **Frontend**: Component tests with React Testing Library; mock API calls for isolation
- **Validation**: Test both frontend form validation and backend constraint validation

### Testing Tools & jsdom note
- **jsdom requirement**: Vitest + React Testing Library tests run in a Node-like DOM (jsdom). If you see failures like "document is not defined" or DOM APIs missing, install `jsdom` in the `frontend` workspace:

```bash
cd frontend
npm install --save-dev jsdom@^27
```

- **Why this matters**: Some CI images or minimal dependency sets omit `jsdom`. The tests in `frontend/src/__tests__/` import `@testing-library/jest-dom/vitest` which relies on a DOM environment provided by `jsdom`.
- **Quick test run**: after installing, run:

```bash
cd frontend
npm test
```

If you'd like, I can add `jsdom` to `frontend/package.json` devDependencies and run `npm install` for you (confirm before I run installs).