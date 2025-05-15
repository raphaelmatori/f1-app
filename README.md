# F1 World Champions Application

This application displays Formula 1 World Champions and provides detailed information about races and winners from 1950 onwards.

## Project Structure

The project is divided into two main parts:

### Backend (Spring Boot)
- Java 17
- Spring Boot 3.4.5
- MySQL 8.0
- JPA/Hibernate
- OpenAPI/Swagger documentation

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
4. Angular CLI 19.2.12

## Setup Instructions

### Database Setup
1. Create a MySQL database named `f1_champions`
2. Update database credentials in `backend/src/main/resources/application.properties` if needed

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
- [ ] Backend API Development
- [ ] Database Schema Design
- [ ] Frontend Components
- [ ] Integration with Ergast API
- [ ] Unit Tests
- [ ] End-to-End Tests
- [ ] Documentation
- [ ] Deployment Instructions

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details 