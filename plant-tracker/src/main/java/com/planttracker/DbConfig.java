// DbConfig.java
package com.planttracker;

public class DbConfig {
    // Reads from environment variables: PLANTDB_URL, PLANTDB_USER, PLANTDB_PASS
    public static String getJdbcUrl() {
        return System.getenv().getOrDefault("PLANTDB_URL", "jdbc:mysql://localhost:3306/PlantDB");
    }
    public static String getUser() {
        return System.getenv().getOrDefault("PLANTDB_USER", "root");
    }
    public static String getPassword() {
        return System.getenv().getOrDefault("PLANTDB_PASS", "Kq37wCXH");
    }
}