package com.planttracker.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the PlantTracker service layer.
 * This starts the REST API that exposes the BusinessManager.
 */

/**
 * ==============================
 * PLANT TRACKER SERVICE HOSTING
 * ==============================
 *
 * This service is deployed on the Render cloud platform.
 *
 * Platform:
 *   - Render (https://render.com)
 *   - Service type: Web Service (Docker-based deployment)
 *   - Public URL: https://csce548.onrender.com
 *
 * Deployment Steps:
 *   1. Push the project to GitHub.
 *   2. Create a new Web Service on Render.
 *   3. Select "Docker" as the environment.
 *   4. Set the Root Directory to the project folder.
 *   5. Provide a Dockerfile for container build.
 *   6. Configure environment variables:
 *        - PLANTDB_URL
 *        - PLANTDB_USER
 *        - PLANTDB_PASS
 *   7. Deploy and monitor logs in Render dashboard.
 *
 * Database Hosting:
 *   - PostgreSQL database hosted on Render (managed database service).
 *   - Tables created manually using SQL Shell (psql).
 *   - Database accessed via JDBC connection string.
 *
 * Environment Variables:
 *   The application reads DB credentials using:
 *     System.getenv("PLANTDB_URL")
 *     System.getenv("PLANTDB_USER")
 *     System.getenv("PLANTDB_PASS")
 *
 * These are securely stored in the Render dashboard and are NOT
 * hard-coded into the application.
 *
 * Testing:
 *   The console-based client (ConsoleApp) invokes the REST endpoints:
 *     POST /api/plants
 *     GET  /api/plants/{id}
 *     PUT  /api/plants/{id}
 *     DELETE /api/plants/{id}
 *
 * This demonstrates full CRUD functionality of the deployed service.
 */

@SpringBootApplication(scanBasePackages = "com.planttracker")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}