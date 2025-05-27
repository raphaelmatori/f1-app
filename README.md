# F1 World Champions Application

This application displays Formula 1 World Champions and provides detailed information about races and winners from 1950 onwards.

## Project Structure

The project is divided into two main parts:

### Backend (Spring Boot)
- Java 17
- Spring Boot 3.4.5
- MySQL 8.0 (main profile)
- H2 in-memory database (test profile)
- JPA/Hibernate
- Redis (for caching)
- OpenAPI/Swagger documentation
- Robust error handling with global exception handler
- Test coverage enforced with Jacoco

### Frontend (Angular)
- Angular 19.2
- SCSS for styling
- Responsive design
- Modern UI/UX practices

## Prerequisites

Before running the application, make sure you have the following installed:

1. Java 17 or higher
2. Node.js 18 or higher
3. MySQL 8.0
4. Redis
5. Angular CLI 19.2.12

## Setup Instructions

### Database Setup
1. Create a MySQL database named `f1_champions`
2. Update database credentials in `backend/src/main/resources/application.yml` if needed

### Backend Setup
1. Navigate to the backend directory:
   ```bash
   cd backend
   ```
2. Build the project:
   ```bash
   ./gradlew build
   ```
3. Run the application:
   ```bash
   ./gradlew bootRun
   ```
The backend will start on http://localhost:8080

#### Running Tests
- The backend uses a test profile with H2 and Redis (localhost:6379).
- To run all tests and check coverage:
  ```bash
  ./gradlew test
  ```
- Jacoco HTML coverage report will be generated at:
  ```
  backend/build/reports/jacoco/test/html/index.html
  ```

### Frontend Setup
1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   ng serve
   ```
The frontend will be available at http://localhost:4200

## API Documentation

Once the backend is running, you can access the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Features

- [x] Project Setup
- [x] Backend API Development (Spring Boot, REST, error handling)
- [x] Database Schema Design (JPA/Hibernate, MySQL, H2 for tests)
- [x] Redis Caching Integration
- [x] Integration with Ergast API
- [x] Unit Tests and Coverage Enforcement (≥ 70% for critical logic)
- [x] OpenAPI/Swagger Documentation
- [x] Graceful error and loading states (backend)
- [x] Race winner data persisted and served from backend after first fetch
- [x] Season list: Display each season's World Champion (2005 to present)
- [x] Race winners: Clicking a season reveals all grand‑prix winners for that year
- [x] Highlight champion in race list
- [x] Frontend Components (Angular SPA: season list, race list, highlight champion, error/loading states)
- [x] End-to-End Tests (frontend and backend integration)
- [x] CI/CD pipeline (GitHub Actions or similar: install → lint → test → build, reject on test failure)
- [x] Dockerization: Multi-stage Dockerfiles, single docker-compose.yml for backend, DB, (optionally frontend)
- [ ] Healthchecks & environment variables in Docker
- [ ] Documentation: High-level architecture, API contract/schema, screenshots/diagrams
- [ ] Deployment Instructions (docker compose up, pipeline triggers)
- [x] Containerized admin tool (e.g., pgAdmin, optional)
- [ ] Security: Dependency scan (CodeQL/Snyk/Trivy), reject on scan failure
- [x] Automatic deploy to free tier platform (Render, Railway, Fly.io, etc.)
- [x] Docker image pushed to public registry (optional)
- [ ] Async job to refresh seasons weekly after every race (optional, nice to have)
- [ ] SSR/SSG (Next.js/Nuxt)
- [x] Lighthouse score ≥ 90 (optional, nice to have)
- [ ] Seed script for DB (optional)

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details 