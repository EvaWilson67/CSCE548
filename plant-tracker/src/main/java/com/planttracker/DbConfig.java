package com.planttracker;

//jdbc:postgresql://localhost:5432/plantdb

//postgres

/**
 * ========================================
 * DATABASE CONFIGURATION (CLOUD DEPLOYMENT)
 * ========================================
 *
 * This class retrieves database configuration from environment variables.
 *
 * When deployed on Render:
 *   - PLANTDB_URL is set to:
 *       jdbc:postgresql://<render-host>:5432/<database>
 *   - PLANTDB_USER is set to the Render database username
 *   - PLANTDB_PASS is set to the Render database password
 *
 * This allows the application to connect securely to the hosted
 * PostgreSQL database without hardcoding credentials.
 *
 * For local development:
 *   If environment variables are not set, fallback values are used.
 */

public class DbConfig {

    public static String getJdbcUrl() {
        return System.getenv().getOrDefault(
            "PLANTDB_URL",
            "jdbc:postgresql://dpg-d6d72sp4tr6s73cklc7g-a.ohio-postgres.render.com:5432/planttracker_db"
        );
    }

    public static String getUser() {
        return System.getenv().getOrDefault("PLANTDB_USER", "planttracker_db_user");
    }

    public static String getPassword() {
        return System.getenv().getOrDefault("PLANTDB_PASS", "");
    }
}