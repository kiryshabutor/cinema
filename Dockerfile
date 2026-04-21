# syntax=docker/dockerfile:1.7

# Frontend build stage
FROM node:20-alpine AS frontend-build
WORKDIR /frontend

COPY frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci

COPY frontend ./
RUN npm run build

# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests -Dcheckstyle.skip dependency:go-offline

COPY config ./config
COPY src ./src
COPY --from=frontend-build /frontend/dist ./src/main/resources/static/app
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -DskipTests -Dcheckstyle.skip package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
