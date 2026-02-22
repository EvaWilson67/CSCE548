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
# Copy the JAR built by the builder stage; adjust filename if needed
COPY --from=builder /build/target/planttracker-service-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]