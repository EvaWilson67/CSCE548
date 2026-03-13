package com.planttracker;


/* 
If you are doing this local (business and data if you don't have access to render password):

PLANTDB_URL: jdbc:postgresql://localhost:5432/plantdb
PLANTDB_USER: postgres
*/ 

/*
If you are doing this via render (serivce):

PLANTDB_URL: jdbc:postgresql://bhnjibolguuj9qrqwpr6-postgresql.services.clever-cloud.com:50013/bhnjibolguuj9qrqwpr6?sslmode=require
PLANTDB_USER: uh8t1i1ozprgavi7ezlr
*/ 


//jdbc:postgresql://localhost:5432/plantdb

//postgres

//To run the Business layer and the DAO, you need to change the password... (I am not putting the password for the DB here...)

public class DbConfig {

    public static String getJdbcUrl() {
        return System.getenv().getOrDefault(
            "PLANTDB_URL",
            "jdbc:postgresql://bhnjibolguuj9qrqwpr6-postgresql.services.clever-cloud.com:50013/bhnjibolguuj9qrqwpr6?sslmode=require"
        );
    }

    public static String getUser() {
        return System.getenv().getOrDefault("PLANTDB_USER", "uh8t1i1ozprgavi7ezlr");
    }

    public static String getPassword() {
        return System.getenv().getOrDefault("PLANTDB_PASS", "");
    }
}