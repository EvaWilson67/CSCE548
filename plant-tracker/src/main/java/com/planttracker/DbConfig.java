package com.planttracker;

//jdbc:postgresql://localhost:5432/plantdb

//postgres



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