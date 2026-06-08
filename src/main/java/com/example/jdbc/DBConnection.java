package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/studentdb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "Dodong23"; // change to your actual password

    public static Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            System.out.println("Attempting connection to: " + URL);
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected successfully!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found in classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Error: " + e.getMessage());
            System.err.println("Error code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return null;
    }
}