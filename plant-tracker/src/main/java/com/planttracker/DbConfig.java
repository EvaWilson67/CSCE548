package com.planttracker;

public class DbConfig {

    public static String getJdbcUrl() {
        return System.getenv().getOrDefault(
            "PLANTDB_URL",
            "jdbc:postgresql://localhost:5432/plantdb"
        );
    }

    public static String getUser() {
        return System.getenv().getOrDefault("PLANTDB_USER", "postgres");
    }

    public static String getPassword() {
        return System.getenv().getOrDefault("PLANTDB_PASS", "Kq37wCXH");
    }
}