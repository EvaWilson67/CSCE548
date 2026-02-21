package com.planttracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtil {

    // No-arg getConnection uses DbConfig so callers (DAOs) don't need DB details
    public static Connection getConnection() throws SQLException {
        String url = DbConfig.getJdbcUrl();
        String user = DbConfig.getUser();
        String pass = DbConfig.getPassword();
        return DriverManager.getConnection(url, user, pass);
    }

    // Keep legacy signature for backward compatibility if any code still uses it
    public static Connection getConnection(String url, String user, String pass) throws SQLException {
        return DriverManager.getConnection(url, user, pass);
    }
}