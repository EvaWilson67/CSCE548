package com.planttracker;


/* 
If you are doing this local (business and data if you don't have access to render password):

PLANTDB_URL: jdbc:postgresql://localhost:5432/plantdb
PLANTDB_USER: postgres
*/ 

/*
If you are doing this via render (serivce):

PLANTDB_URL: jdbc:postgresql://dpg-d6d72sp4tr6s73cklc7g-a.ohio-postgres.render.com:5432/planttracker_db
PLANTDB_USER: planttracker_db_user
*/ 


//jdbc:postgresql://localhost:5432/plantdb

//postgres

//To run the Business layer and the DAO, you need to change the password... (I am not putting the password for the DB here...)

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
        return System.getenv().getOrDefault("PLANTDB_PASS", "Q4CDV5HxAPQtE7Gb1ysI7Rrgy2K3iMSR");
    }
}