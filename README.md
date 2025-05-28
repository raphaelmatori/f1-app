# F1 World Champions Application

A modern web application that displays Formula 1 World Champions and provides detailed information about races and winners from 1950 onwards. The application features real-time data updates, caching mechanisms, and a responsive user interface.

## System Architecture

### Overview
The application follows a microservices architecture with the following components:

![Hight Level Architecture](docs/images/high-level-architecture.png)

### Frontend (Angular)
- Angular 19.2
- Standalone Components Architecture
- SCSS for styling
- Responsive design with modern UI/UX practices
- Service abstraction with interfaces
- Lazy-loaded modules
- HTTP interceptors for API communication

### Backend (Spring Boot)
- Java 17
- Spring Boot 3.4.5
- Spring Data JPA/Hibernate
- Spring Cache with Redis
- OpenAPI/Swagger documentation
- Global exception handling
- Scheduled tasks for data updates
- Health monitoring with Spring Actuator

### Data Storage
- MySQL 8.0 for persistent storage
- Redis for caching (race results and champions data)
- Two-layer caching strategy (Redis + Caffeine)

### Integration
- REST API communication between frontend and backend
- CORS configuration for security
- Ergast F1 API integration with retry mechanism
- Health checks for all services

## API Contract

### Champions API
```
GET /api/v1/champions
Response: List<ChampionDTO>
{
  "year": number,
  "driverId": string,
  "givenName": string,
  "familyName": string,
  "nationality": string
}
```

### Races API
```
GET /api/v1/races/{year}
Response: List<RaceDTO>
{
  "season": number,
  "round": number,
  "raceName": string,
  "date": string,
  "circuit": {
    "circuitName": string,
    "location": {
      "country": string
    }
  },
  "results": [{
    "driver": {
      "driverId": string,
      "givenName": string,
      "familyName": string
    },
    "constructor": {
      "constructorId": string,
      "name": string
    }
  }]
}
```

## Prerequisites

- Java 17 or higher
- Node.js 18 or higher
- Docker and Docker Compose
- Angular CLI 19.2.12 (for local development)

## Development Setup

The easiest way to run the application in development mode is using Docker Compose:

```bash
docker compose -p f1-champions-backend -f docker-compose.dev.yml up --build
```

This will start:
- Backend (http://localhost:8080)
- Frontend (http://localhost:4000)
- MySQL database
- Redis cache
- Adminer (http://localhost:8081)

### Manual Development Setup

#### Backend Setup
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

#### Frontend Setup
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

## Production Deployment

### Building and Publishing Backend Docker Image

To build and publish a new production backend image:

```bash
# Navigate to backend directory
cd backend/

# Build the application
./gradlew build

# Build the Docker image
docker build -t f1-champions-backend:prod .

# Tag the image for Docker Hub
docker tag f1-champions-backend:prod raphaelmatori/f1-champions-backend:prod

# Push to Docker Hub
docker push raphaelmatori/f1-champions-backend:prod
```

### Running the Production Backend

The backend requires MySQL and Redis. Make sure both services are running and accessible. Then run:

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e MYSQL_PORT=<port> \
  -e MYSQL_HOST=<host> \
  -e MYSQL_USER=<user> \
  -e MYSQL_PASSWORD=<password> \
  -e MYSQL_DATABASE=<database> \
  -e REDIS_HOST=<host> \
  -e REDIS_PORT=<port> \
  -e REDIS_PASSWORD=<password> \
  raphaelmatori/f1-champions-backend:prod
```

Replace the placeholders with your actual configuration values.

## Environment Variables

### Backend
- SPRING_PROFILES_ACTIVE: Application profile (dev/prod)
- MYSQL_HOST: MySQL server hostname
- MYSQL_PORT: MySQL server port
- MYSQL_DATABASE: Database name
- MYSQL_USER: Database username
- MYSQL_PASSWORD: Database password
- REDIS_HOST: Redis server hostname
- REDIS_PORT: Redis server port
- REDIS_PASSWORD: Redis password (optional)

## Testing

### Backend Tests
Run the backend tests with:
```bash
cd backend
./gradlew test
```

The test report will be generated at:
\`backend/build/reports/jacoco/test/html/index.html\`

### Frontend Tests
Run the frontend tests with:
```bash
cd frontend
npm test
```

## API Documentation

Once the backend is running, you can access the API documentation at:
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Health Monitoring

The application includes comprehensive health checks for all services:

- Backend Health: http://localhost:8080/actuator/health
- MySQL Health: Monitored via backend health checks
- Redis Health: Monitored via backend health checks
- Frontend Health: Basic HTTP check on port 4000

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details 