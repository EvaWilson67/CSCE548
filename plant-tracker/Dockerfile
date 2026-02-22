# ======================================================
# Docker Configuration for Render Deployment
# ======================================================
#
# This Dockerfile builds and runs the Spring Boot service
# on the Render cloud platform.
#
# Build Stage:
#   - Uses Maven to compile and package the application.
#
# Runtime Stage:
#   - Uses lightweight JRE image.
#   - Executes the Spring Boot jar.
#
# The application reads database credentials from
# environment variables configured in the Render dashboard.
#
# Render automatically builds this container during deployment.

# Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app
# copy built jar (use wildcard if your jar name may vary)
COPY --from=builder /build/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]