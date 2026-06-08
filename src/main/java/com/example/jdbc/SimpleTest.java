package com.example.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;

public class SimpleTest {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/studentdb";
        String user = "postgres";
        String password = "Dodong23";  // CHANGE to your actual PostgreSQL password

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("✅ Connection SUCCESS!");
            conn.close();
        } catch (Exception e) {
            System.out.println("❌ Connection FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}