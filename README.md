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

## License
MIT (adjust as needed).
