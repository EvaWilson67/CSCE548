package com.planttracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbUtil {
    private DbUtil() {}

    public static Connection getConnection(String jdbcUrl, String user, String password) throws SQLException {
        // optional explicit driver loading:
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ignored) {}
        return DriverManager.getConnection(jdbcUrl, user, password);
    }
}
