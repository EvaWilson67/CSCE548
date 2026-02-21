package com.planttracker.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the PlantTracker service layer.
 * This starts the REST API that exposes the BusinessManager.
 */
@SpringBootApplication(scanBasePackages = "com.planttracker")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}