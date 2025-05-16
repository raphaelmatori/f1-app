# Build stage
FROM gradle:jdk17 AS build
WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon -x test

# Run stage
FROM openjdk:17-slim
WORKDIR /app
COPY --from=build /app/build/libs/app-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"] 
